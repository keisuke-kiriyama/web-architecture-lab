package com.example.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 認証フィルター
 *
 * 【学習ポイント】
 * Spring Security はフィルターチェーンでリクエストを処理する。
 * このフィルターは各リクエストで JWT を検証し、
 * 有効なら SecurityContext にユーザー情報をセットする。
 *
 * OncePerRequestFilter を継承することで、
 * リクエストごとに1回だけ実行されることが保証される。
 *
 * 【処理の流れ】
 * 1. Authorization ヘッダーから JWT を取得
 * 2. JWT を検証
 * 3. 有効なら UserDetailsService でユーザー情報を取得
 * 4. SecurityContext に Authentication をセット
 * 5. 以降の処理（Controller 等）で Principal として使える
 *
 * 【Reviewer観点】
 * - Authorization ヘッダーの形式チェックは適切か
 * - 検証失敗時に適切なエラーレスポンスを返しているか
 * - SecurityContext のクリアは適切か
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    /**
     * フィルター処理
     *
     * 【学習ポイント】
     * FilterChain.doFilter() を呼ぶと次のフィルターに処理が移る。
     * 呼ばないとリクエストがそこで止まる。
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // Authorization ヘッダーから JWT を取得
        String token = extractToken(request);

        // トークンが存在し、有効な場合
        if (token != null && jwtUtil.validateToken(token)) {
            // JWT からユーザー名を取得
            String username = jwtUtil.getUsername(token);

            // UserDetailsService でユーザー情報を取得
            // 【学習ポイント】
            // JWT にはユーザー名しか入っていないので、
            // 権限情報等は DB から取得する必要がある。
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // Authentication オブジェクトを作成
            // 【学習ポイント】
            // UsernamePasswordAuthenticationToken の3引数コンストラクタは
            // 「認証済み」を意味する。2引数だと「未認証」。
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,           // principal
                            null,                  // credentials（JWT認証では不要）
                            userDetails.getAuthorities()  // 権限
                    );

            // リクエストの詳細情報を設定（IP アドレス等）
            authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );

            // SecurityContext に設定
            // 【学習ポイント】
            // これにより、以降の処理で authentication.getPrincipal() や
            // @AuthenticationPrincipal でユーザー情報を取得できる。
            // Controller の引数 Principal も同様。
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // 次のフィルターへ
        // 【学習ポイント】
        // JWT が無効でも次に進む（403 を返すのは認可フィルターの仕事）。
        // このフィルターは「認証情報の設定」のみを担当。
        filterChain.doFilter(request, response);
    }

    /**
     * Authorization ヘッダーから Bearer トークンを抽出
     *
     * 【学習ポイント】
     * JWT は通常、Authorization ヘッダーに以下の形式で送信される：
     * Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
     *
     * "Bearer " プレフィックスを除去してトークン部分のみ返す。
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        // "Bearer " で始まる場合、トークン部分を抽出
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }
}
