package com.example.dbclpm.filter;

import java.io.IOException;
import java.util.Objects;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.example.dbclpm.dto.ResponseCommonDto;
import com.google.gson.Gson;

@WebFilter(urlPatterns = {"/api/teacher/*", "/api/student/*"})
public class APITeacherAndStudentFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        // TODO Auto-generated method stub
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        HttpSession session = req.getSession();
        
        if (req.getRequestURI().contains("login")
        		|| req.getRequestURI().contains("login")
        		|| req.getRequestURI().contains("forgot-password")
        		|| req.getRequestURI().contains("reset-password")) {
            chain.doFilter(req, resp);
            return;
        }
        
        if ((Objects.nonNull(session.getAttribute("teacher")) || Objects.nonNull(session.getAttribute("student")))
        		&& !req.getRequestURI().contains("login")
        		&& !req.getRequestURI().contains("forgot-password")
        		&& !req.getRequestURI().contains("reset-password")) {
            chain.doFilter(req, resp);
            return;
        }
        
        resp.setStatus(401);
        resp.getWriter().println(new Gson().toJson(new ResponseCommonDto("UnAuthorize", 401, null)));
    }
}
