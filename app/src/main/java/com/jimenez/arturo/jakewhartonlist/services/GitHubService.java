package com.jimenez.arturo.jakewhartonlist.services;

import com.jimenez.arturo.jakewhartonlist.models.RepoModel;

import java.util.List;

import retrofit.http.GET;
import retrofit.http.Path;

public interface GitHubService {

    @GET("/users/{username}/repos?page=1&per_page=10")
    List<RepoModel> listRepos(@Path("username") String username);
}
