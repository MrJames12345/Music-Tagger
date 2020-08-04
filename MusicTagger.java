import java.util.*;
import java.io.*;
 // Mp3 file tagging
import com.mpatric.mp3agic.*;


class MusicTagger
{

// - Variables - //

        String songsLocation;
        File[] songFiles;
        String imageLocation;


// - Set Foler - //


        public void setFolder( String inSongsLocation )
        {
                songsLocation = inSongsLocation;
                songFiles = new File( songsLocation ).listFiles();
                imageLocation = System.getProperty( "user.home" ) + "\\Desktop\\tempImage.jpg";
        }


// - Setting Tags - //


        public void setTags( String inFileName, HashMap<String, String> inTags, OutputBox inBox ) throws IOException, FileNotFoundException, SongAlreadyProcessedException
        {

                // Finding original file
                boolean check = false;
                File oldFile = null;
                String tempFileName = null;
                String fileNameToCompare = toggleFileNameSuitable( inFileName ) + ".mp3";
                String newFileName = toggleFileNameSuitable( inTags.get( "artist" ) ) + " - " + toggleFileNameSuitable( inTags.get( "title" ) ) + ".mp3";
                // New file
                Mp3File newFile = null;
                ID3v2 tagsObject = null;
                // Other
                int i;

                try
                {

                        // Go through files until found file
                        inBox.update( "> Searching files..." );
                        for ( i = 0; i < songFiles.length && !check; i++ )
                        {

                                oldFile = songFiles[i];
                                tempFileName = oldFile.getName();
System.out.println( "tempFileName: " + tempFileName );
System.out.println( "fileNameToCompare: " + fileNameToCompare );
                                // IF found file, set check
                                if ( tempFileName.equals( fileNameToCompare ) )
                                {
                                        check = true;
                                }
                                // ELSE IF found file already processed, throw exception
                                else if ( tempFileName.equals( newFileName ) )
                                {
                                        throw new SongAlreadyProcessedException();
                                }

                        }

                        // IF didn't find file, throw exception
                        if ( !check )
                        {
                                throw new FileNotFoundException();
                        }
                        // ELSE set tags
                        else
                        {

                                inBox.update( "> Found file!" );

                                // Create mp3 file
                                newFile = new Mp3File( oldFile );
                                // Create tags object
                                tagsObject = newFile.getId3v2Tag();

                                // Set title, artist and album
                                inBox.update( "> Setting tags...");
                                tagsObject.setTitle( inTags.get( "title" ) );
                                tagsObject.setArtist( inTags.get( "artist" ) );
                                tagsObject.setAlbum( inTags.get( "album" ) );
                                // Get artwork and set to tags
                                try
                                {
                                        tagsObject.setAlbumImage( downloadArtworkAsBytes( inTags ), "image/jpg" );
                                }
                                // IF getting image failed, throw exception, so add all tags except image
                                catch( UnsupportedImageException e )
                                {
                                        inBox.update( "> Unable to get artwork! Set other tags though." );
                                }

                                // Set these tags to new mp3 file
                                newFile.setId3v2Tag( tagsObject );
                                // Save new file as "<artist> - <title>"
                                newFile.save( songsLocation + "\\" + newFileName );
                                // Delete old file
                                oldFile.delete();

                        }

                }
                catch ( UnsupportedTagException | InvalidDataException | NotSupportedException e )
                {
                        throw new IOException();
                }

        }


// - Private Methods - //


        private File downloadArtwork( HashMap<String, String> inTags ) throws IOException, UnsupportedImageException
        {

                GoogleImagesScraper googleScraper = new GoogleImagesScraper();
                String imageUrl = null;

                // Make search
                googleScraper.setSearch( inTags.get( "artist" ) + " " + inTags.get( "album" ), 10 );

                // Get 1st image result's url
                imageUrl = googleScraper.getImageUrl( 1 );

                // Download image
                ImageDownloader.downloadImage( imageUrl, imageLocation );

                // Return downloaded image as file
                return new File( imageLocation );

        }


        private byte[] downloadArtworkAsBytes( HashMap<String, String> inTags ) throws IOException, UnsupportedImageException
        {

                GoogleImagesScraper googleScraper = new GoogleImagesScraper();
                String imageUrl = null;
                byte[] outBytes = null;

                // Make search
                googleScraper.setSearch( inTags.get( "artist" ) + " " + inTags.get( "album" ), 10 );

                // Get 1st image result's url
                imageUrl = googleScraper.getImageUrl( 1 );

                // Download image
                outBytes = ImageDownloader.downloadImageAsBytes( imageUrl );

                // Return downloaded image as file
                return outBytes;

        }


        // Make given text suitable to be compared to file names created by 4K Video Downloader
        private String toggleFileNameSuitable( String inString )
        {
                String outString = inString;

                outString.replace( "/" , " " );
                outString.replace( ":" , " " );
                outString.replace( "\"", " " );
                outString.replace( "<" , " " );
                outString.replace( ">" , " " );
                outString.replace( "?" , ""  );

                return outString;
        }

}