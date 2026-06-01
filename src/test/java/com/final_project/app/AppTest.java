package com.final_project.app;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.final_project.app.Download;
/**
 * Unit test for simple App.
 */
public class AppTest {

    /**
     * Rigorous Test :-)
     */
    @Test
    public void test_download() {
        Download download = new Download("https://www.youtube.com/watch?v=jHZrrV5m6dI", 
                                        "./", "mp3", false, true);
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
            Download test = new Download("url", file_type, "Along the Road to Gundagai", true);
            success[incr] = test.convert();
            incr ++;
            if (!test.check_for_corruption()) {
                assertTrue(true);
            }
        }
        for ( boolean val: success ) {
            if (val) {
                str += "a";
            }
        }
        assertTrue(str.equals(strt));
    }

    @Test
    public void test_playlist() {
        Download download = new Download("https://www.youtube.com/watch?v=MRKy3kX8XUM&list=PLecKPCyj4yRP8P_v2XXxP85CGaXxs4LWw",
                                        "./", "mp3", true, true);
        if (download.download() &&
               download.convert() &&
               download.merge()) {
                System.out.println("check");
            assertTrue(download.check_for_corruption());
        } else {
            assertTrue(false);
        }
    }

}
