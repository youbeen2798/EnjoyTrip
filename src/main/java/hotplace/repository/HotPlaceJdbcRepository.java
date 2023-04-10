package hotplace.repository;

import attraction.AttractionInfo;
import hotplace.HotPlace;
import hotplace.UploadFile;
import hotplace.dto.HotPlaceSearch;
import member.Member;
import util.DBConnectionUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static member.Authority.ADMIN;
import static member.Authority.CLIENT;

public class HotPlaceJdbcRepository implements HotPlaceRepository {

    private static final HotPlaceRepository hotPlaceRepository = new HotPlaceJdbcRepository();
    private final DBConnectionUtil dbConnectionUtil;

    private HotPlaceJdbcRepository() {
        dbConnectionUtil = DBConnectionUtil.getInstance();
    }

    public static HotPlaceRepository getHotPlaceRepository() {
        return hotPlaceRepository;
    }

    @Override
    public int save(HotPlace hotPlace) {
        int count = 0;
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = dbConnectionUtil.getConnection();
            String sql = "insert into hot_place(member_id, content_id, content_type_id, name, `desc`, visited_date, upload_file_name, store_file_name) values (?, ?, ?, ?, ?, ?, ?, ?)";

            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, hotPlace.getMember().getId());
            pstmt.setInt(2, hotPlace.getAttractionInfo().getId());
            pstmt.setInt(3, hotPlace.getContentTypeId());
            pstmt.setString(4, hotPlace.getName());
            pstmt.setString(5, hotPlace.getDesc());
            pstmt.setString(6, hotPlace.getVisitedDate());
            pstmt.setString(7, hotPlace.getUploadFile().getUploadFileName());
            pstmt.setString(8, hotPlace.getUploadFile().getStoreFileName());

            count = pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            dbConnectionUtil.close(pstmt, conn);
        }
        return count;
    }

    @Override
    public Optional<HotPlace> findById(Long hotPlaceId) {
        HotPlace hotPlace = null;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = dbConnectionUtil.getConnection();
            String sql = "select * from hot_place where hot_place_id = ?;";

            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, hotPlaceId);

            rs = pstmt.executeQuery();
            if (rs.next()) {
                hotPlace = createHotplace(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            dbConnectionUtil.close(rs, pstmt, conn);
        }
        return Optional.ofNullable(hotPlace);
    }

    @Override
    public List<HotPlace> findByCondition(HotPlaceSearch condition) {
        List<HotPlace> hotPlaces = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = dbConnectionUtil.getConnection();
            String sql = "select * from hot_place hp" +
                    " join member m" +
                    " on hp.member_id = m.member_id" +
                    " where m.nickname like ?" +
                    " or hp.name like ?" +
                    " or hp.desc like ?";

            if (condition.getSortCondition() == 2) {
                sql += " order by hp.hit desc";
            } else {
                sql += " order by hp.created_date desc";
            }

            //작성자, 제목, 내용
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, '%' + condition.getName() + '%');
            pstmt.setString(2, '%' + condition.getName() + '%');
            pstmt.setString(3, '%' + condition.getName() + '%');

            rs = pstmt.executeQuery();
            while (rs.next()) {
                hotPlaces.add(createJoinHotPlace(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            dbConnectionUtil.close(rs, pstmt, conn);
        }
        return hotPlaces;
    }

    @Override
    public int update(HotPlace hotPlace) {
        int count = 0;
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = dbConnectionUtil.getConnection();
            String sql = "update hot_place set name=?, `desc`=?, visited_date=?, last_modified_date=? where hot_place_id=?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, hotPlace.getName());
            pstmt.setString(2, hotPlace.getDesc());
            pstmt.setString(3, hotPlace.getVisitedDate());
            pstmt.setTimestamp(4, Timestamp.valueOf(hotPlace.getLastModifiedDate()));
            pstmt.setLong(5, hotPlace.getId());

            count = pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            dbConnectionUtil.close(pstmt, conn);
        }
        return count;
    }

    @Override
    public int updateHit(Long hotPlaceId, HotPlace hotPlace) {
        int count = 0;
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = dbConnectionUtil.getConnection();
            String sql = "update hot_place set hit=? where hot_place_id=?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, hotPlace.getHit());
            pstmt.setLong(2, hotPlaceId);

            count = pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            dbConnectionUtil.close(pstmt, conn);
        }
        return count;
    }

    @Override
    public int remove(Long hotPlaceId) {
        int count = 0;
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = dbConnectionUtil.getConnection();
            String sql = "delete from hot_place where hot_place_id = ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, hotPlaceId);

            count = pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            dbConnectionUtil.close(pstmt, conn);
        }
        return count;
    }

    @Override
    public void clear() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = dbConnectionUtil.getConnection();
            String sql = "delete from hot_place";

            pstmt = conn.prepareStatement(sql);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            dbConnectionUtil.close(pstmt, conn);
        }
    }

    private HotPlace createHotplace(ResultSet rs) throws SQLException {
        return HotPlace.builder()
                .id(rs.getLong("hot_place_id"))
                .name(rs.getString("name"))
                .desc(rs.getString("desc"))
                .hit(rs.getInt("hit"))
                .visitedDate(rs.getString("visited_date"))
                .uploadFile(
                        UploadFile.builder()
                                .uploadFileName(rs.getString("upload_file_name"))
                                .storeFileName(rs.getString("store_file_name"))
                                .build()
                )
                .createdDate(rs.getTimestamp("created_date").toLocalDateTime())
                .lastModifiedDate(rs.getTimestamp("last_modified_date").toLocalDateTime())
                .contentTypeId(rs.getInt("content_type_id"))
                .member(Member.builder()
                        .id(rs.getLong("member_id"))
                        .build())
                .attractionInfo(
                        AttractionInfo.builder()
                                .id(rs.getInt("content_id"))
                                .build()
                )
                .build();
    }

    private HotPlace createJoinHotPlace(ResultSet rs) throws SQLException {
        return HotPlace.builder()
                .id(rs.getLong("hot_place_id"))
                .name(rs.getString("name"))
                .desc(rs.getString("desc"))
                .hit(rs.getInt("hit"))
                .visitedDate(rs.getString("visited_date"))
                .uploadFile(
                        UploadFile.builder()
                                .uploadFileName(rs.getString("upload_file_name"))
                                .storeFileName(rs.getString("store_file_name"))
                                .build()
                )
                .createdDate(rs.getTimestamp("created_date").toLocalDateTime())
                .lastModifiedDate(rs.getTimestamp("last_modified_date").toLocalDateTime())
                .contentTypeId(rs.getInt("content_type_id"))
                .member(createMember(rs))
                .attractionInfo(
                        AttractionInfo.builder()
                                .id(rs.getInt("content_id"))
                                .build()
                )
                .build();
    }

    private Member createMember(ResultSet rs) throws SQLException {
        return Member.builder()
                .id(rs.getLong("member_id"))
                .loginId(rs.getString("login_id"))
                .loginPw(rs.getString("login_pw"))
                .username(rs.getString("username"))
                .email(rs.getString("email"))
                .phone(rs.getString("phone"))
                .birth(rs.getString("birth"))
                .gender(rs.getString("gender"))
                .nickname(rs.getString("nickname"))
                .nicknameLastModifiedDate(
                        rs.getTimestamp("nickname_last_modified_date").toLocalDateTime()
                )
                .authority(
                        rs.getString("authority").equals("CLIENT") ? CLIENT : ADMIN
                )
                .createdDate(rs.getTimestamp("created_date").toLocalDateTime())
                .lastModifiedDate(rs.getTimestamp("last_modified_date").toLocalDateTime())
                .build();
    }
}
