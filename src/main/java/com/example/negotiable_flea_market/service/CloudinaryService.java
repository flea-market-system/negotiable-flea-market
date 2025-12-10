// src/main/java/com/example/fleamarketsystem/service/CloudinaryService.java
package com.example.negotiable_flea_market.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

	// Cloudinary クライアントの参照
	private final Cloudinary cloudinary;

	// 必要な認証情報をコンストラクタインジェクションで受け取る
	public CloudinaryService(
			@Value("${cloudinary.cloud-name}") String cloudName,
			@Value("${cloudinary.api-key}") String apiKey,
			@Value("${cloudinary.api-secret}") String apiSecret) {
		// 渡された資格情報で Cloudinary クライアントを初期化
		this.cloudinary = new Cloudinary(ObjectUtils.asMap(
				"cloud_name", cloudName,
				"api_key", apiKey,
				"api_secret", apiSecret));
	}

	// 画像をアップロードして公開 URL を返す（空ファイルは null）
	public String uploadFile(MultipartFile file) throws IOException {
		// アップロードなしのケースは null を返す
		if (file.isEmpty()) {
			return null;
		}

		// バイト配列をそのままアップロード（オプションは既定）
		@SuppressWarnings("rawtypes")
		Map uploadResult = cloudinary.uploader().upload(
				file.getBytes(),
				ObjectUtils.emptyMap());

		// 返却 Map から公開 URL を取り出して返す
		return uploadResult.get("url").toString();
	}

	// Cloudinary 上のリソースを削除（URL から public_id を推定）
	public void deleteFile(String publicId) throws IOException {
		// URL を / で分割して末尾のファイル名を取り出す
		String[] parts = publicId.split("/");
		String fileName = parts[parts.length - 1];

		// 拡張子を除いた public_id を推定
		String publicIdWithoutExtension = fileName.substring(0, fileName.lastIndexOf('.'));

		// public_id を指定して削除 API を呼び出す
		cloudinary.uploader().destroy(
				publicIdWithoutExtension,
				ObjectUtils.emptyMap());
	}
}
