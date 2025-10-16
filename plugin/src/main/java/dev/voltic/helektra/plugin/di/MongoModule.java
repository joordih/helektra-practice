package dev.voltic.helektra.plugin.di;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import dev.voltic.helektra.plugin.utils.LoggerUtils;
import dev.voltic.helektra.plugin.utils.config.FileConfig;

public class MongoModule extends AbstractModule {

  private final FileConfig settingsConfig;

  public MongoModule(FileConfig settingsConfig) {
    this.settingsConfig = settingsConfig;
  }

  static {
    LoggerUtils.blockMongoDBLogs();
  }

  @Provides
  @Singleton
  public MongoClient provideClient() {
    try {
      String mongoUri = settingsConfig.getConfig().getString("settings.mongodb.uri");
      MongoClient client = MongoClients.create(mongoUri);

      return client;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  @Provides
  @Singleton
  public MongoDatabase provideDatabase(MongoClient client) {
    String databaseName = settingsConfig.getConfig().getString("settings.mongodb.database");
    return client.getDatabase(databaseName);
  }
}
