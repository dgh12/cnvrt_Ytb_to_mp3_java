package com.final_project.app;

import java.util.Scanner;

public class GetUserInput {
    private String url;
    private Boolean download_as_playlist;
    private String file_path;
    private String file_type;
    private Boolean delete = true;
    private boolean merge;
    private Scanner input = new Scanner(System.in);

    public GetUserInput() {

    }

    public GetUserInput(String url, String file_path,
                        Boolean download_as_playlist, String file_type) {
        setDownload_as_playlist(download_as_playlist);
        setFile_path(file_path);
        setUrl(url);
        setFile_type(file_type);
    }

    public void setUrl(String url) {
        this.url = url;
    }
    public String getUrl() {
        return url;
    }
    public void setDownload_as_playlist(Boolean download_as_playlist) {
        this.download_as_playlist = download_as_playlist;
    }
    public Boolean getDownload_as_playlist() {
        return download_as_playlist;
    }
    public void setFile_path(String file_path) {
        this.file_path = file_path;
    }
    public String getFile_path() {
        return file_path;
    }
    public void setFile_type(String file_type) {
        this.file_type = file_type;
    }
    public String getFile_type() {
        return file_type;
    }
    public void setDelete(Boolean delete) {
        this.delete = delete;
    }
    public Boolean getDelete() {
        return delete;
    }
    public void setMerge(boolean merge) {
        this.merge = merge;
    }
    public boolean getMerge() {
        return merge;
    }
    public void getUserInput() {
        System.out.print("Please enter the url: ");
        this.setUrl(input.nextLine());
        System.out.println("Please enter the desired filetype: (only types; mp3, acc, flac, pcm_s16le, and pcm_u8 are allowed.) ");
        this.setFile_type(input.nextLine());
        System.out.print("Do you want to download as a playlist? [y/N]");
        String download_as_playlist = input.nextLine();
        if (download_as_playlist.equals("")) {
            download_as_playlist = "N";
        }
        this.setDownload_as_playlist(download_as_playlist.toLowerCase().equals("y"));
        this.setFile_path("./");
        if (this.getDownload_as_playlist()) {
            System.out.print("Do you want to merge the playlist videos into one: [Y/n]");
            String merge = input.nextLine();
            if (merge.equals("")) {
                merge = "y";
            }
            this.setMerge(merge.toLowerCase().equals("y"));
            if (this.getMerge()) {
                System.out.print("Do you want to delete the individual playlist files: [Y/n]");
                String delete = input.nextLine();
                if (delete.equals("")) {
                    delete = "y";
                }
                this.setDelete(delete.toLowerCase().equals("y"));
            }
        }
    }
    public void success() {
        System.out.println();
        System.out.println("File downloaded successfully.");
    }
    public void fail() {
        System.out.println();
        System.out.println("Failed to download file.");
    }
}
