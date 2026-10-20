package com.college.eventmanager.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * Handles database configuration loading, environment overrides, and persistence.
 */
public class DbConfig {
    private static final String DEFAULT_PROPERTIES_FILE = "db.properties";
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 3306;
    private static final String DEFAULT_NAME = "college_event_db";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "";

    private String host;
    private int port;
    private String name;
    private String user;
    private String password;
    private String explicitUrl;

    public DbConfig() {
        load();
    }

    public synchronized void load() {
        Properties props = new Properties();

        // 1. Try loading from working directory first
        File localFile = new File(DEFAULT_PROPERTIES_FILE);
        boolean loaded = false;
        if (localFile.exists()) {
            try (FileInputStream fis = new FileInputStream(localFile)) {
                props.load(fis);
                loaded = true;
            } catch (Exception ignored) {
            }
        }

        // 2. Fall back to classpath resource
        if (!loaded) {
            try (InputStream is = getClass().getClassLoader().getResourceAsStream(DEFAULT_PROPERTIES_FILE)) {
                if (is != null) {
                    props.load(is);
                    loaded = true;
                }
            } catch (Exception ignored) {
            }
        }

        this.host = props.getProperty("db.host", DEFAULT_HOST);
        try {
            this.port = Integer.parseInt(props.getProperty("db.port", String.valueOf(DEFAULT_PORT)));
        } catch (NumberFormatException e) {
            this.port = DEFAULT_PORT;
        }
        this.name = props.getProperty("db.name", DEFAULT_NAME);
        this.user = props.getProperty("db.user", DEFAULT_USER);
        this.password = props.getProperty("db.password", DEFAULT_PASSWORD);
        this.explicitUrl = props.getProperty("db.url", "");

        // 3. Environment variable overrides (if present)
        String envHost = System.getenv("DB_HOST");
        if (envHost != null && !envHost.isBlank()) this.host = envHost;

        String envPort = System.getenv("DB_PORT");
        if (envPort != null && !envPort.isBlank()) {
            try { this.port = Integer.parseInt(envPort); } catch (NumberFormatException ignored) {}
        }

        String envName = System.getenv("DB_NAME");
        if (envName != null && !envName.isBlank()) this.name = envName;

        String envUser = System.getenv("DB_USER");
        if (envUser != null && !envUser.isBlank()) this.user = envUser;

        String envPass = System.getenv("DB_PASSWORD");
        if (envPass != null) this.password = envPass;

        String envUrl = System.getenv("DB_URL");
        if (envUrl != null && !envUrl.isBlank()) this.explicitUrl = envUrl;
    }

    public synchronized void save(String host, int port, String name, String user, String password) throws Exception {
        this.host = host;
        this.port = port;
        this.name = name;
        this.user = user;
        this.password = password;
        this.explicitUrl = "";

        Properties props = new Properties();
        props.setProperty("db.host", host);
        props.setProperty("db.port", String.valueOf(port));
        props.setProperty("db.name", name);
        props.setProperty("db.user", user);
        props.setProperty("db.password", password);
        props.setProperty("db.url", getJdbcUrl());

        File localFile = new File(DEFAULT_PROPERTIES_FILE);
        try (FileOutputStream fos = new FileOutputStream(localFile)) {
            props.store(fos, "Saved by College Event and Workshop Manager GUI");
        }
    }

    public String getJdbcUrl() {
        if (explicitUrl != null && !explicitUrl.isBlank()) {
            return explicitUrl;
        }
        return String.format(
            "jdbc:mysql://%s:%d/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&createDatabaseIfNotExist=true",
            host, port, name
        );
    }

    public String getHost() { return host; }
    public int getPort() { return port; }
    public String getName() { return name; }
    public String getUser() { return user; }
    public String getPassword() { return password; }
}
