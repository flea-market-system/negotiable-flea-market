package com.example.negotiable_flea_market;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//★以下の2つのインポートを追加してください
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import io.github.cdimascio.dotenv.Dotenv;


@SpringBootApplication
public class NegotiableFleaMarketApplication {

	public static void main(String[] args) {
		// ★ここから追加：.envを読み込んでシステムプロパティにセットする処理
        try {
            Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
            dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
        } catch (Exception e) {
            // .envファイルがなくてもエラーにせず進む（本番環境の環境変数などに対応するため）
        }
        // ★ここまで
		SpringApplication.run(NegotiableFleaMarketApplication.class, args);
	}
	// ★このメソッドを追加してください
    // これで「RestTemplateを使っていいよ」とSpringに登録されます
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
