package com.frontend.HospitalManagement.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
class HomeController{

    @GetMapping
    public String getHome(){
        return "index";
    }

}