package com.final_project.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
/**
 * Unit test for simple App.
 */
public class AppTest {

    @AfterAll
    public static void delete_files() throws IOException{
        String[] files = {
            "Along the Road to Gundagai.mp4",
            "Along the Road to Gundagai.mp3",
            "Along the Road to Gundagai.m4a",
            "Along the Road to Gundagai.wav",
            "Bach   Violin Sonata no  1 in G minor BWV 1001   Sato   Netherlands Bach Society.mp4",
            "Bach   Violin Sonata no  1 in G minor BWV 1001   Sato   Netherlands Bach Society.mp3",
            "Bach   Violin Partita no  1 in B minor BWV 1002   Sato   Netherlands Bach Society.mp4",
            "Bach   Violin Partita no  1 in B minor BWV 1002   Sato   Netherlands Bach Society.mp3",
            "Bach   Violin Sonata no  2 in A minor BWV 1003   Sato   Netherlands Bach Society.mp4",
            "Bach   Violin Sonata no  2 in A minor BWV 1003   Sato   Netherlands Bach Society.mp3",
            "Bach   Violin Partita no  2 in D minor BWV 1004   Sato   Netherlands Bach Society.mp4",
            "Bach   Violin Partita no  2 in D minor BWV 1004   Sato   Netherlands Bach Society.mp3",
            "Bach   Violin Sonata no  3 in C major BWV 1005   Sato   Netherlands Bach Society.mp4",
            "Bach   Violin Sonata no  3 in C major BWV 1005   Sato   Netherlands Bach Society.mp3",
            "Bach   Violin Partita no  3 in E major BWV 1006   Sato   Netherlands Bach Society.mp4",
            "Bach   Violin Partita no  3 in E major BWV 1006   Sato   Netherlands Bach Society.mp3",
            "merge.txt",
            "Six sonatas and partitas for solo violin.mp3",
        };
        for (String file : files) {
            File to_delete = new File(file);
            Files.deleteIfExists(to_delete.toPath());
        };
    }

    /**
     * Rigorous Test :-)
     */
    @Test
    public void test_download() {
        Download download = new Download("https://www.youtube.com/watch?v=jHZrrV5m6dI", 
                                         "mp3", false);
        assertTrue(download.download());
    }

    @Test
    public void test_conversion() {
        String[] file_types = {"mp3", "m4a", "pcm_s16le", "pcm_u8"};
        boolean[] success = new boolean[4];
        int incr = 0;
        String str = "";
        String strt = "aaaa";
        for (String file_type : file_types) {
            Download test = new Download("url", file_type, "Along the Road to Gundagai");
            success[incr] = test.convert();
            incr ++;
            assertTrue(test.check_for_corruption());
        }
        for ( boolean val: success ) {
            if (val) {
                str += "a";
            }
        }
        assertEquals(str, strt);
    }

    @Test
    public void test_playlist() {
        Download download = new Download("https://www.youtube.com/watch?v=MRKy3kX8XUM&list=PLecKPCyj4yRP8P_v2XXxP85CGaXxs4LWw",
                                         "mp3", true);
        if (download.download() &&
               download.convert() &&
               download.merge()) {
            assertTrue(download.check_for_corruption());
        } else {
            fail();
        }
    }

}
