package com.jidian.cosalon.migration.pos365.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class Pos365Service {

    @Autowired
    private RestTemplate restTemplate;


}
