package com.isb.library.web.controller;

import org.apache.commons.compress.utils.IOUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

@RestController
public class FaviconController {

    /**
     * Method to retrieve the favicon of the website
     * @param response
     */
    @RequestMapping("favicon.ico")
    public void getFavicon(HttpServletResponse response) {
        try {
            InputStream is = getClass().getResourceAsStream("/static/favicon.ico");
            response.setContentType("image/x-icon");
            IOUtils.copy(is, response.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


