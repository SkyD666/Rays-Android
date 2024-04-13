package com.skyd.rays.api;


interface IApiService {
    String searchStickers(String requestPackage, String keyword, int startIndex, int size);
}