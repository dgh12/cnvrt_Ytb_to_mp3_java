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
import io.github.kinsleykajiva.ffmpeg.model.SampleRate;;

public class Download {
    private String url;
    private String file_path;
    private AudioCodec codec_type;
    private String file_type;
    private boolean download_as_playlist;
    private boolean test = false;
    private boolean delete = false;
    private String title;
    private ArrayList<String> files = new ArrayList<>();
    
    public Download (String url, String file_path,
        boolean download_as_playlist, String file_type, boolean delete) {
            this.url = url;
            this.file_path = file_path;
            this.download_as_playlist = download_as_playlist;
            this.codec_type = translate_codac_type(file_type);
            this.file_type = translate_file_type(file_type);
            this.delete = delete;
        }
    
    public Download (String url, String file_path,
                    String file_type, boolean download_as_playlist, boolean test) {
            this.url = url;
            this.file_path = file_path;
            this.download_as_playlist = download_as_playlist;
            this.codec_type = translate_codac_type(file_type);
            this.file_type = translate_file_type(file_type);
            this.test = test;
        }
    
    public Download (String url, String file_type, String file, boolean test) {
        this.url = url;
        this.files.add(file);
        this.file_type = translate_file_type(file_type);
        this.codec_type = translate_codac_type(file_type);  
        this.test = test;
    }
        
    private AudioCodec translate_codac_type(String file_type) {
        HashMap<String, AudioCodec> file_types = new HashMap<>();
        file_types.put("mp3", AudioCodec.LIBMP3LAME);//mp3
        file_types.put("m4a", AudioCodec.AAC);//acc
        file_types.put("flac", AudioCodec.FLAC);//flac
        file_types.put("pcm_s16le", AudioCodec.PCM_S16LE);//wav
        file_types.put("pcm_u8", AudioCodec.PCM_S16LE);//wav
        for (var key : file_types.keySet()) {
            if (key.equals(file_types)) {
                return file_types.get(key);
            }
        }
        return file_types.get("mp3");
    }

    private String translate_file_type(String file_type) {
        if (file_type.equals("pcm_s16le") || file_type.equals("pcm_u8")) {
            return "wav";
        }
        return file_type;
    }
    
    public boolean download() throws JSONException{
        if (download_as_playlist) {
            return download_playlist();
        } else {
            return download_url();
        }
    }
    
    private boolean download_url() {
        try  {
            Youtube yt = new Youtube(this.url);
            String file_name = sanitize(yt.getTitle());
            System.out.println(file_name);
            yt.streams().getOnlyAudio().download(this.file_path, file_name);
            System.out.println();
            this.files.add(file_name);
            return true;
        } catch (Exception e) {
            System.err.println(e);
            return false;
        }
    }

    private boolean download_playlist() throws JSONException{
        try {
            Playlist playlist = new Playlist(this.url);
            this.title = sanitize(playlist.getTitle());
            for (String url : playlist.getVideos()) {
                Youtube yt = new Youtube(url);
                String file_name = sanitize(yt.getTitle());
                System.out.println(file_name);
                yt.streams().getOnlyAudio().download(this.file_path, file_name);
                this.files.add(file_name);
                System.out.println("\n");
            }
            return true;
        } catch (Exception e) {
            System.err.println(e);
            return false;
        }
    }

    private String sanitize(String filename) {
        String nfilename = Normalizer.normalize(filename, Normalizer.Form.NFC);
        return nfilename.replaceAll("[\"'#$%*,.:;<>?\\\\^|~/]", " ").replace("-", " ");
    }

    public boolean convert() {
        try {
            for (String file : this.files) {
                System.out.println("Converting '" + file + ".mp4' to '" + file + "." + file_type + "'");
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

                if (!this.test) {
                    File to_delete = new File(file + ".mp4");
                    Files.deleteIfExists(to_delete.toPath());
                }

                System.out.println(" Deleted original file.");
            }
            return true;
        } catch (Exception e) {
            System.err.println(e);
            return false;
        }
    }

    public boolean check_for_corruption() {
        try {
            for (String file : this.files) {
                String[] cmd = {
                    FFmpegBinary.getFfprobe().getAbsolutePath(),
                    "-v", "error",
                    file + "." + this.file_type,
                };
                
                ProcessBuilder FFprobe_build =  new ProcessBuilder(cmd);
                FFprobe_build.inheritIO();
                Process FFprobe_process = FFprobe_build.start();
                int exitCode = FFprobe_process.waitFor();
                if (exitCode != 0) {
                    return false;
                }
            }
        } catch (Exception e){
            System.err.println(e);
            return false;
        }
    return true;
    }
    
    public boolean merge() {
        File merge_file = new File("merge.txt");
        ArrayList<String> merge_files = new ArrayList<>();
        for (String file : this.files) {
            merge_files.add("file '" + file + "." + this.file_type + "'");
        }
        try {
            Files.write(merge_file.toPath(), merge_files, StandardOpenOption.CREATE);
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
            ProcessBuilder FFmpeg_build = new ProcessBuilder(cmd);
            FFmpeg_build.redirectErrorStream(true);
            Process FFmpeg_process = FFmpeg_build.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(FFmpeg_process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            FFmpeg_process.getInputStream().close();
            FFmpeg_process.getErrorStream().close();
            int exitCode = FFmpeg_process.waitFor();
            System.out.println("waited");
            System.out.println(exitCode);

            if (exitCode == 0) {
                System.out.println("added file");
                if (!this.test || this.delete) {
                    for (String file : files) {
                        File to_delete = new File(file + "." + this.file_type);
                        Files.delete(to_delete.toPath());
                    }
                }
                return true;
            } else {
                System.err.println("FFmpeg exited with an error code of: " + exitCode);
                return false; 
            }
        } catch (Exception e) {
            System.err.println(e);
            return false;
        }
    }
}