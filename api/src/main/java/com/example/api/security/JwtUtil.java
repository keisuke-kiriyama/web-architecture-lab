package com.example.api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT の生成・検証を行うユーティリティクラス
 *
 * 【学習ポイント】
 * JWT（JSON Web Token）は3つの部分で構成される：
 * 1. Header: アルゴリズム情報（alg: HS256）
 * 2. Payload: ユーザー情報、有効期限等（Base64エンコード、暗号化ではない）
 * 3. Signature: Header + Payload を秘密鍵で署名
 *
 * 署名により改ざんを検知できる。
 * Payload は誰でも読めるので、機密情報は入れない。
 *
 * 【Reviewer観点】
 * - 秘密鍵の長さは十分か（HMAC-SHA256 には 256bit 以上必要）
 * - 有効期限は設定されているか
 * - 秘密鍵はハードコードされていないか
 * - トークン検証時に例外を適切に処理しているか
 */
@Component
public class JwtUtil {

    /**
     * 署名に使う秘密鍵
     *
     * 【学習ポイント】
     * @Value で application.yml の値を注入できる。
     * ${JWT_SECRET:default} の形式で環境変数 → デフォルト値の順に取得。
     */
    private final SecretKey secretKey;

    /**
     * トークンの有効期限（ミリ秒）
     */
    private final long expiration;

    /**
     * コンストラクタ
     *
     * 【学習ポイント】
     * HMAC-SHA256 アルゴリズムには 256bit（32バイト）以上の鍵が必要。
     * 短い文字列を使うとセキュリティリスクになる。
     *
     * Keys.hmacShaKeyFor() で文字列から SecretKey を生成。
     */
    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expiration) {
        // 秘密鍵を SecretKey オブジェクトに変換
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
    }

    /**
     * JWT を生成する
     *
     * 【学習ポイント】
     * JWT の構造:
     * eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyMSIsImlhdCI6MTcwODUwMDAwMCwiZXhwIjoxNzA4NTAzNjAwfQ.署名
     * └───── Header ─────┘ └────────────────── Payload ──────────────────┘ └ Sig ┘
     *
     * - sub (subject): 主題（ここではユーザー名）
     * - iat (issued at): 発行日時
     * - exp (expiration): 有効期限
     *
     * @param username ユーザー名
     * @return 生成された JWT 文字列
     */
    public String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                // subject: JWT の主題（ユーザー識別子）
                .subject(username)
                // issued at: 発行日時
                .issuedAt(now)
                // expiration: 有効期限
                .expiration(expiryDate)
                // 署名アルゴリズムと秘密鍵を指定
                // 【学習ポイント】
                // signWith() で HMAC-SHA256 署名を追加。
                // この署名があるから改ざんを検知できる。
                .signWith(secretKey)
                // 文字列に変換
                .compact();
    }

    /**
     * JWT を検証してユーザー名を取得する
     *
     * 【学習ポイント】
     * 検証では以下をチェック：
     * 1. 署名が正しいか（改ざんされていないか）
     * 2. 有効期限が切れていないか
     *
     * どちらかが NG なら JwtException がスローされる。
     *
     * @param token JWT 文字列
     * @return ユーザー名
     * @throws JwtException 検証失敗時
     */
    public String getUsername(String token) {
        Claims claims = Jwts.parser()
                // 署名検証に使う秘密鍵を設定
                .verifyWith(secretKey)
                .build()
                // トークンをパース（署名検証 + 有効期限チェック）
                .parseSignedClaims(token)
                // Payload を取得
                .getPayload();

        return claims.getSubject();
    }

    /**
     * JWT が有効かどうかを検証する
     *
     * 【学習ポイント】
     * 検証に失敗すると JwtException（またはそのサブクラス）がスローされる。
     * - SignatureException: 署名が不正
     * - ExpiredJwtException: 有効期限切れ
     * - MalformedJwtException: JWT の形式が不正
     *
     * @param token JWT 文字列
     * @return 有効なら true
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException e) {
            // 署名不正、有効期限切れ、形式不正など
            // 【学習ポイント】
            // 本番ではログに記録するが、詳細なエラー内容は
            // クライアントに返さない（情報漏洩防止）。
            return false;
        }
    }
}
