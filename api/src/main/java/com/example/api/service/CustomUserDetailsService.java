package com.example.api.service;

import com.example.api.entity.User;
import com.example.api.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Spring Security の認証で使用する UserDetailsService の実装
 *
 * 【学習ポイント】
 * Spring Security が認証を行う際、UserDetailsService.loadUserByUsername() を呼ぶ。
 * このメソッドで DB からユーザーを取得し、UserDetails オブジェクトに変換する。
 *
 * 流れ:
 * 1. ユーザーがログインフォームで username/password を送信
 * 2. Spring Security が loadUserByUsername(username) を呼ぶ
 * 3. DB からユーザーを取得して UserDetails を返す
 * 4. Spring Security が UserDetails.getPassword() と入力パスワードを比較
 * 5. PasswordEncoder.matches() でハッシュ比較
 *
 * 【Reviewer観点】
 * - ユーザーが見つからない場合に UsernameNotFoundException を投げているか
 * - パスワードを平文で比較していないか（PasswordEncoder が行う）
 * - 適切な権限（GrantedAuthority）が設定されているか
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * コンストラクタインジェクション
     *
     * 【学習ポイント】
     * Spring では @Autowired を使ったフィールドインジェクションより
     * コンストラクタインジェクションが推奨される。
     * 理由：テスト時にモックを注入しやすい、イミュータブル。
     */
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * ユーザー名でユーザーを検索し、UserDetails に変換
     *
     * @param username ログインフォームで入力されたユーザー名
     * @return UserDetails（Spring Security が認証に使用）
     * @throws UsernameNotFoundException ユーザーが見つからない場合
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // DB からユーザーを検索
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // Spring Security の User オブジェクトに変換
        // 【学習ポイント】
        // UserDetails インターフェースの実装として
        // org.springframework.security.core.userdetails.User を使用。
        //
        // 第3引数は権限（ロール）のリスト。認可で使用する。
        // 例: hasRole("ADMIN") で管理者のみアクセス可能にできる。
        // 今回は全ユーザーに ROLE_USER を付与（権限分岐は作らない）。
        return new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            user.getPassword(),  // BCrypt ハッシュ済みのパスワード
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}
