package com.final_project.app;

// main runner class to run the program
public class Runner {
    // main method to run the program
    public static void main(String[] args) throws Exception{
        // initialize variables to hold user input and success of the download, conversion, and merge
        GetUserInput input = new GetUserInput();
        input.getUserInput();
        String url = input.getUrl();
        Boolean download_as_playlist = input.getDownload_as_playlist();
        String file_type = input.getFile_type();
        Boolean delete = input.getDelete();
        boolean merge = input.getMerge();
        String success = "f";
        
        // create a new download object with the user input
        Download download = new Download(url, download_as_playlist, file_type, delete);
        
        // if the url downloads and the conversion is successful...
        if (download.download() && download.convert()) {
            // set succes to a to indicate suceessful download and conversion
            success = "a";
        }
        // if the user wants to merge the files...
        if (merge) {
            // it the file merged successfully...
            if (download.merge()) {
                // add a to success to indicate a successful merge
                success += "a";
            }
        // otherwise...
        } else {
            // add a to success to keep the succes variable passing
            // successful download and conversion but no merge
            success += "a";
        }

        // if the files are not corrupted and the success variable is aa indicating a successful download...
        if(download.check_for_corruption() && success.equals("aa")) {
            // print a success message
            input.success();
        } else {
            // otherwise, print a failure message
            input.fail();
        }
    }
}