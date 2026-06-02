package com.final_project.app;

import java.util.Scanner;
import java.util.Set;

// class to get user input for the program
public class GetUserInput {
    // initialize variables to hold user input
    private String url;
    private Boolean download_as_playlist;
    private String file_type;
    private Boolean delete = true;
    private boolean merge;
    private Scanner input = new Scanner(System.in);

    // constructor
    public GetUserInput() {

    }

    // getters and setters for the user input variables
        // getter and setter for url
        public void setUrl(String url) {
            this.url = url;
        }
        public String getUrl() {
            return url;
        }
        // getter and setter for download_as_playlist
        public void setDownload_as_playlist(Boolean download_as_playlist) {
            this.download_as_playlist = download_as_playlist;
        }
        public Boolean getDownload_as_playlist() {
            return download_as_playlist;
        }
        // getter and setter for file_type
        public void setFile_type(String file_type) {
            this.file_type = file_type;
        }
        public String getFile_type() {
            return file_type;
        }
        // getter and setter for delete
        public void setDelete(Boolean delete) {
            this.delete = delete;
        }
        public Boolean getDelete() {
            return delete;
        }
        // getter and setter for merge
        public void setMerge(boolean merge) {
            this.merge = merge;
        }
        public boolean getMerge() {
            return merge;
        }
    
    // method to get user input for the program
    public void getUserInput() {
        System.out.print("Please enter the url: ");
        this.setUrl(input.nextLine());

        // the file type is empty if it is not valid.
        String type = "";
        // loop until the user enters a valid file type
        while (type.length() == 0) {
            System.out.print("Please enter the desired filetype: \n(only types; mp3, acc, pcm_s16le, and pcm_u8 are allowed.) ");
            // set of valid file types
            Set<String> file_types = Set.of(
                "mp3", "acc", "pcm_s16le", "pcm_u8"
            );
            type = input.nextLine();
            // check if the entered file type is valid
            if (file_types.contains(type)){
                this.setFile_type(type);
            // otherwise, print an error message and loop again
            } else {
                System.out.println("The entered filetype: " + type + "is not accepted.");
                type = "";
            }
        }

        System.out.print("Do you want to download as a playlist? [y/N]");
        String download_as_playlist = input.nextLine();
        // if the user just presses enter, set the value to n
        if (download_as_playlist.equals("")) {
            download_as_playlist = "n";
        }
        this.setDownload_as_playlist(download_as_playlist.toLowerCase().equals("y"));

        // if the user wants to download as a playlist, ask if they want to merge the files and delete the individual files
        if (this.getDownload_as_playlist()) {
            System.out.print("Do you want to merge the playlist videos into one: [Y/n]");
            String merge = input.nextLine();
            // if the user just presses enter, set the value to y
            if (merge.equals("")) {
                merge = "y";
            }
            this.setMerge(merge.toLowerCase().equals("y"));

            // if the user wants to merge the files, ask if they want to delete the individual files
            if (this.getMerge()) {
                System.out.print("Do you want to delete the individual playlist files: [Y/n]");
                String delete = input.nextLine();
                // if the user just presses enter, set the value to y
                if (delete.equals("")) {
                    delete = "y";
                }
                this.setDelete(delete.toLowerCase().equals("y"));
            }
        }
    }

    // method to print a success message
    public void success() {
        System.out.println();
        System.out.println("File downloaded successfully.");
    }

    // method to print a failure message
    public void fail() {
        System.out.println();
        System.out.println("Failed to download file.");
    }
}
