package member.repository;

import member.Member;
import member.dto.MemberAddDto;
import util.DBConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class MemberJdbcRepository implements MemberRepository {

    private static final MemberRepository memberRepository = new MemberJdbcRepository();
    private final DBConnectionUtil dbConnectionUtil;

    private MemberJdbcRepository() {
        dbConnectionUtil = DBConnectionUtil.getInstance();
    }

    public static MemberRepository getMemberRepository() {
        return memberRepository;
    }

    @Override
    public void save(MemberAddDto memberAddDto) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = dbConnectionUtil.getConnection();
            String sql = "insert into member(login_id, login_pw, username, email, phone, nickname, birth, gender)" +
                    " values (?, ?, ?, ?, ?, ?, ?, ?);";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, memberAddDto.getLoginId());
            pstmt.setString(2, memberAddDto.getLoginPw());
            pstmt.setString(3, memberAddDto.getUsername());
            pstmt.setString(4, memberAddDto.getEmail());
            pstmt.setString(5, memberAddDto.getPhone());
            pstmt.setString(6, memberAddDto.getNickname());
            pstmt.setString(7, memberAddDto.getBirth());
            pstmt.setString(8, memberAddDto.getGender());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            dbConnectionUtil.close(pstmt, conn);
        }
    }

    @Override
    public Optional<Member> findById(Long memberId) {
        Member member = null;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = dbConnectionUtil.getConnection();
            String sql = "select * from member where member_id = ?;";

            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, memberId);

            rs = pstmt.executeQuery();
            if (rs.next()) {
                member = createMember(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            dbConnectionUtil.close(rs, pstmt, conn);
        }
        return Optional.ofNullable(member);
    }

    @Override
    public Optional<Member> findByLoginId(String loginId) {
        Member member = null;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = dbConnectionUtil.getConnection();
            String sql = "select * from member where login_id = ?;";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, loginId);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                member = createMember(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            dbConnectionUtil.close(rs, pstmt, conn);
        }
        return Optional.ofNullable(member);
    }

    @Override
    public void clear() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = dbConnectionUtil.getConnection();
            String sql = "delete from member;";

            pstmt = conn.prepareStatement(sql);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            dbConnectionUtil.close(pstmt, conn);
        }
    }

    private Member createMember(ResultSet rs) throws SQLException {
        return new Member(
                rs.getLong("member_id"),
                rs.getString("login_id"),
                rs.getString("login_pw"),
                rs.getString("username"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("nickname"),
                rs.getString("birth"),
                rs.getString("gender"),
                rs.getTimestamp("created_date").toLocalDateTime(),
                rs.getTimestamp("last_modified_date").toLocalDateTime()
        );
    }
}