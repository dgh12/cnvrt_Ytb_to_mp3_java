package com.final_project.app;

public class Runner {

    public static void main(String[] args) throws Exception{
        GetUserInput input = new GetUserInput();
        input.getUserInput();
        String url = input.getUrl();
        Boolean download_as_playlist = input.getDownload_as_playlist();
        String file_path = input.getFile_path();
        String file_type = input.getFile_type();
        Boolean delete = input.getDelete();
        
        Download download = new Download(url, file_path, download_as_playlist, file_type, delete);

        if(download.download() && download.convert()) {
            input.success();
        } else {
            input.fail();
        }
    }
}