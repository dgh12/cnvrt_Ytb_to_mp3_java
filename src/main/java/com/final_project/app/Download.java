package com.final_project.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

import org.json.JSONException;

import com.github.felipeucelli.javatube.Playlist;
import com.github.felipeucelli.javatube.Youtube;

import io.github.kinsleykajiva.ffmpeg.FFmpeg;
import io.github.kinsleykajiva.ffmpeg.FFmpegBinary;
import io.github.kinsleykajiva.ffmpeg.model.AudioCodec;
import io.github.kinsleykajiva.ffmpeg.model.EncodingResult;
import io.github.kinsleykajiva.ffmpeg.model.SampleRate;


public class Download {
    // innitiate variables
    private String url;
    private String file_type;
    private AudioCodec codec_type;
    private boolean download_as_playlist;
    private boolean delete;
    private boolean test = true;
    private String title;
    private ArrayList<String> files = new ArrayList<>();
    
    // constructors
    public Download (String url,
        boolean download_as_playlist, String file_type, boolean delete) {
            this.url = url;
            this.download_as_playlist = download_as_playlist;
            this.codec_type = translate_codec_type(file_type);
            this.file_type = translate_file_type(file_type);
            this.delete = delete;
            this.test = false;
        }
    
    public Download (String url,
                    String file_type, boolean download_as_playlist) {
            this.url = url;
            this.download_as_playlist = download_as_playlist;
            this.codec_type = translate_codec_type(file_type);
            this.file_type = translate_file_type(file_type);
        }
    
    public Download (String url, String file_type, String file) {
        this.url = url;
        this.files.add(file);
        this.file_type = translate_file_type(file_type);
        this.codec_type = translate_codec_type(file_type);  
    }
    
    // methods
    // utitlty method to tranlate file type to codec type for ffmpeg
    private AudioCodec translate_codec_type(String file_type) {
        // HashMap to translate file types to codec types for ffmpeg
        HashMap<String, AudioCodec> file_types = new HashMap<>();
        file_types.put("mp3", AudioCodec.LIBMP3LAME);//mp3
        file_types.put("m4a", AudioCodec.AAC);//acc
        file_types.put("flac", AudioCodec.FLAC);//flac
        file_types.put("pcm_s16le", AudioCodec.PCM_S16LE);//wav
        file_types.put("pcm_u8", AudioCodec.PCM_U8);//wav
        for (var key : file_types.keySet()) {
            if (key.equals(file_type)) {
                return file_types.get(key);
            }
        }
        return file_types.get("mp3");
    }

    // utility method to translate file type to file extension for ffmpeg
    private String translate_file_type(String file_type) {
        // if the file type is pcm_s16le or pcm_u8, the output file type should be wav
        if (file_type.equals("pcm_s16le") || file_type.equals("pcm_u8")) {
            return "wav";
        }
        //otherwise the output file type is the same as the provided file type
        return file_type;
    }
    
    // method to streamline the download process
    public boolean download() throws JSONException{
        // if the user decided to download as a playlist...
        if (download_as_playlist) {
            // download as a playlist
            return download_playlist();
        } else {
            // otherwise, download as a single url
            return download_url(this.url);
        }
    }
    
    // method to download a single URL
    private boolean download_url(String url) {
        // try to download the url except...
        try  {
            // initialize downloader object
            Youtube yt = new Youtube(url);
            // get its file name and sanitize it to remove any characters that may cause issues with ffmpeg
            String file_name = sanitize(yt.getTitle());
            System.out.println(file_name);
            // download the file and add it to the list of downloaded files
            yt.streams().getOnlyAudio().download("./", file_name);
            System.out.println();
            this.files.add(file_name);
            return true;
        // if there is an error, print the error and return false
        } catch (Exception e) {
            System.err.println(e);
            return false;
        }
    }

    // method to download a playlist
    private boolean download_playlist() throws JSONException{
        // try to download the playlist except...
        try {
            // extract the playlist videos
            Playlist playlist = new Playlist(this.url);
            // get the playlist title and sanitize it to remove any characters that may cause issues with ffmpeg
            this.title = sanitize(playlist.getTitle());
            // for each video in the playlist, download the video and add it to the list of downloaded files
            for (String url : playlist.getVideos()) {
                this.download_url(url);
            }
            return true;
        // if there is an error, print the error and return false
        } catch (Exception e) {
            System.err.println(e);
            return false;
        }
    }

    // method to sanitize file names to remove any characters that may cause issues with ffmpeg
    private String sanitize(String filename) {
        String nfilename = Normalizer.normalize(filename, Normalizer.Form.NFC);
        // replace any characters that may cause issues with ffmpeg with a space and replace dashes with spaces to avoid issues with ffmpeg
        return nfilename.replaceAll("[\"'#$%*,.:;<>?\\\\^|~/]", " ").replace("-", " ");
    }

    // method to convert the downloaded files to the desired file type
    public boolean convert() {
        // try to convert the files except...
        try {
            // for each file, convert the file to the desired file type using ffmpeg and the ffmpeg wrapper library
            for (String file : this.files) {
                System.out.println("Converting '" + file + ".mp4' to '" + file + "." + file_type + "'");
                // use the ffmpeg wrapper library to convert the file to the desired file type with the desired codec, bitrate, and sample rate
                CompletableFuture<EncodingResult> future = FFmpeg.input(file + ".mp4")
                .output(file + "." + this.file_type)
                .withCodec(this.codec_type)
                .withBitrate("324k")
                .withSampleRate(SampleRate.SR_48000)
                .executeAsync();

                EncodingResult result = future.join();
                System.out.println(" Output: " + result.outputPath());
                System.out.println(" Size: " + String.format("%.2f MB", result.fileSizeMB()));
                System.out.println(" Duration: " + result.timeTakenMillis() + "ms");

                // if it is not a test and the file is not corrupted, delete the original file
                if (this.check_for_corruption(file) && !this.test) {
                    File to_delete = new File(file + ".mp4");
                    Files.deleteIfExists(to_delete.toPath());
                }
                
                System.out.println(" Deleted original file.");
            }
            return true;
        // if there is an error, print the error and return false
        } catch (Exception e) {
            System.err.println(e);
            return false;
        }
    }

    // method to check for file(s) corruption using ffprobe
    public boolean check_for_corruption() {
        // try to check the files except...
        try {
            for (String file : this.files) {
                // set up the command to run ffprobe on the file to check for corruption
                String[] cmd = {
                    FFmpegBinary.getFfprobe().getAbsolutePath(),
                    "-v", "error",
                    file + "." + this.file_type,
                };
                
                // run the command
                ProcessBuilder FFprobe_build =  new ProcessBuilder(cmd);
                FFprobe_build.inheritIO();
                Process FFprobe_process = FFprobe_build.start();
                int exitCode = FFprobe_process.waitFor();
                //check the exit code to determine if the file is corrupted or not
                if (exitCode != 0) {
                    return false;
                }
            }
        // if there is an error, print the error and return false
        } catch (Exception e){
            System.err.println(e);
            return false;
        }
    // if all of the files are not corrupted, return true
    return true;
    }

    // method to check for file corruption using ffprobe for a single file
    public boolean check_for_corruption(String file) {
        // try to check the files except...
        try {
            // set up the command to run ffprobe on the file to check for corruption
            String[] cmd = {
                FFmpegBinary.getFfprobe().getAbsolutePath(),
                "-v", "error",
                file + "." + this.file_type,
            };
            
            // run the command
            ProcessBuilder FFprobe_build =  new ProcessBuilder(cmd);
            FFprobe_build.inheritIO();
            Process FFprobe_process = FFprobe_build.start();
            int exitCode = FFprobe_process.waitFor();
            //check the exit code to determine if the file is corrupted or not
            if (exitCode != 0) {
                return false;
            }
        // if there is an error, print the error and return false
        } catch (Exception e){
            System.err.println(e);
            return false;
        }
    // if all of the files are not corrupted, return true
    return true;
    }
    
    // method to merge files if the download was a playlist
    public boolean merge() {
        // file to hold the list of files to merge for ffmpeg
        File merge_file = new File("merge.txt");
        // set up the list of files to merge for ffmpeg in the format required by ffmpeg (file 'filename')
        ArrayList<String> merge_files = new ArrayList<>();
        for (String file : this.files) {
            merge_files.add("file '" + file + "." + this.file_type + "'");
        }
        // try to merge the files except...
        try {
            // write the list of files to merge to the merge file
            Files.write(merge_file.toPath(), merge_files, StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING);
            // set up the command to run ffmpeg to merge the files
            String[] cmd = {
                FFmpegBinary.getFfmpeg().getAbsolutePath(),
                "-y",
                "-hide_banner",
                "-f", "concat",
                "-safe", "0",
                "-i", "merge.txt",
                "-c", "copy",
                this.title + "." + this.file_type,
            };
            // run the command
            ProcessBuilder FFmpeg_build = new ProcessBuilder(cmd);
            FFmpeg_build.inheritIO();
            Process FFmpeg_process = FFmpeg_build.start();
            int exitCode = FFmpeg_process.waitFor();
            // check the exit code to determine if the merge was successful or not
            if (exitCode == 0) {
                System.out.println(" Merged files\n");
                // if the user wanted to delete the individual files, delete the individual files and the merge.txt file
                if (this.delete) {
                    for (String file : files) {
                        File to_delete = new File(file + "." + this.file_type);
                        Files.delete(to_delete.toPath());
                        File merge = new File("merge.txt");
                        Files.deleteIfExists(merge.toPath());
                    }
                }
                return true;
            // if the merge was not successful, print an error message and return false
            } else {
                System.err.println("FFmpeg exited with an error code of: " + exitCode);
                return false; 
            }
        // if there is an error, print the error and return false
        } catch (Exception e) {
            System.err.println(e);
            return false;
        }
    }
}