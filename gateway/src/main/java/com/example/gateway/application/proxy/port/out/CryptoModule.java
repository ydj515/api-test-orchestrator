package com.example.gateway.application.proxy.port.out;

public interface CryptoModule {

    String encrypt(String key, String plainJson);

    String decrypt(String key, String encrypted);
}
