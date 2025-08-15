/*
  This file is licensed to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package io.github.valters.xsdiff.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.common.base.Preconditions;
import com.google.common.io.ByteSink;

import io.github.valters.xsdiff.report.HtmlContentOutput;
import io.github.valters.xsdiff.report.HtmlRes;
import io.github.valters.xsdiff.report.XmlDomUtils;
import io.github.valters.xsdiff.report.XmlSchemaDiffReport;

/** entry point */
public class Main {

    public static void main( final String[] args ) {
        if( args.length == 2 || args.length == 3 ) {
            new App().run( args );
        }
        else {
            usage();
        }
    }

    private static void usage() {
        System.out.println( "Usage: xsdiff-app <folder1> <folder2> [report-output-folder]" );
        System.out.println( "   or: xsdiff-app <file1.xsd> <file2.xsd> [report-output-folder]" );
        System.out.println( "When comparing whole folders, a schema.lst 'listing' file must exist in <folder2>." );
    }

    /** app bootstrap */
    public static class App {

        /** list of files to compare: single file name on each line */
        private static final String LISTING_FILE = "schema.lst";
        private static final DateTimeFormatter MINUTESTAMP = DateTimeFormatter.ofPattern( "HHmm" );

        private String reportFolder = "report-" + LocalDate.now().format( DateTimeFormatter.ISO_LOCAL_DATE ) + "-" + LocalTime.now().format( MINUTESTAMP );

        public void run( final String[] args ) {
            try {
                if( args.length == 3 ) {
                    reportFolder = args[2];
                }

                File f1 = Paths.get( args[0] ).toFile();
                File f2 = Paths.get( args[1] ).toFile();

                if( f1.isDirectory() && f2.isDirectory() ) {
                    runDiff( f1.toPath(), f2.toPath(), LISTING_FILE );
                }

                if( f1.isFile() && f2.isFile() ) {
                    runDiff( f1, f2 );
                }

                System.out.println( "done" );
            }
            catch( final Exception e ) {
                System.out.println( "Error, failed to run, exception occurred: " + e );
                e.printStackTrace();
            }
        }

        /** run diff on single file pair */
        void runDiff( final File file1, final File file2 ) throws Exception {

            final File report = new File( reportFolder );
            Preconditions.checkState( report.mkdir(), "Error, failed to create folder '%s'", report );
            System.out.println( "output single file [" + file1 + "/" + file2 + "] comparison to: " + report );

            final Path f1 = file1.toPath();
            final Path f2 = file2.toPath();

            final HtmlContentOutput contentOutput = HtmlContentOutput.startOutput( report, "diff-report-" + file2.getName() + ".html" );

            printFileComparisonHeader( contentOutput, f1, f2 );

            runDiff( Files.newBufferedReader( f1 ),
                    Files.newBufferedReader( f2 ),
                    contentOutput );

            contentOutput.finishOutput();

            writeResources( report );
        }

        /** run diff on two folders, with a listing file */
        void runDiff( final Path folder1, final Path folder2, final String listFilesToCompare ) throws Exception {

            final File report = new File( reportFolder );
            Preconditions.checkState( report.mkdir(), "Error, failed to create folder '%s'", report );
            System.out.println( "output: to folder '" + report + "'" );

            final List<String> fileList = collectLines( folder2.resolve( listFilesToCompare ) );

            for( final String fileName : fileList ) {
                System.out.println( "compare: " + fileName );
                final HtmlContentOutput contentOutput = HtmlContentOutput.startOutput( report, "diff-report-" + fileName + ".html" );

                final Path f1 = folder1.resolve( fileName );
                final Path f2 = folder2.resolve( fileName );

                printFileComparisonHeader( contentOutput, f1, f2 );

                runDiff( Files.newBufferedReader( f1 ),
                        Files.newBufferedReader( f2 ),
                        contentOutput );

                contentOutput.finishOutput();
            }

            writeResources( report );

        }

        private void writeResources( final File report ) throws Exception {

            System.out.println( "html: write " + HtmlRes.ALL_RESOURCES.length + " resources" );
            for( final String res : HtmlRes.ALL_RESOURCES ) {
                try( final InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream( res ) ) {
                    Preconditions.checkNotNull( in, "Failed to read resource %s", res );
                    Path path = Paths.get( res );

                    final File parent = new File( report, path.getName( 1 ).toString() ); // parent folder (css or js)
                    if( !parent.exists() ) {
                        Preconditions.checkState( parent.mkdir(), "Error, failed to create folder '%s'", parent );
                    }
                    final File out = new File( parent, path.getName( 2 ).toString() ); // file name

                    final ByteSink bs = com.google.common.io.Files.asByteSink( out );
                    bs.writeFrom( in );
                }
            }
        }

        void printFileComparisonHeader( final HtmlContentOutput contentOutput, final Path f1, final Path f2 ) {
            contentOutput.startFileHeader();
            contentOutput.write( "comparing: " + f1 + " with " + f2 );
            contentOutput.endFileHeader();
        }

        List<String> collectLines( final Path listFilesToCompare ) {
            try( final BufferedReader br = Files.newBufferedReader( listFilesToCompare ) ) {
                return br.lines().collect( Collectors.toList() );
            }
            catch( final IOException e ) {
                throw new RuntimeException( "Failed to read listing file " + listFilesToCompare + ": " + e, e );
            }
        }

        void runDiff( final Reader file1, final Reader file2, final HtmlContentOutput output ) {

            try {

                final DocumentBuilder docBuilder = XmlDomUtils.documentBuilder();
                final Document controlDoc = docBuilder.parse( new InputSource( file1 ) );
                final Document testDoc = docBuilder.parse( new InputSource( file2 ) );

                new XmlSchemaDiffReport( output ).runDiff( controlDoc, testDoc );
            }
            catch( final ParserConfigurationException | SAXException | IOException e ) {
                throw new RuntimeException( "Failed to parse: ", e );
            }
        }
    }
}
