package com.final_project.app;

public class Runner {

    public static void main(String[] args) throws Exception{
        GetUserInput input = new GetUserInput();
        input.getUserInput();
        String url = input.getUrl();
        Boolean download_as_playlist = input.getDownload_as_playlist();
        String file_type = input.getFile_type();
        Boolean delete = input.getDelete();
        boolean merge = input.getMerge();
        
        Download download = new Download(url, download_as_playlist, file_type, delete);
        
        download.download();
        download.convert();
        if (merge) {
            download.merge();
        }

        if(download.check_for_corruption()) {
            input.success();
        } else {
            input.fail();
        }
    }
}