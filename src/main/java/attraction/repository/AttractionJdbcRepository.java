package attraction.repository;

import attraction.AttractionInfo;
import attraction.dto.AttractionSearch;
import util.DBConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AttractionJdbcRepository implements AttractionRepository {

    private static final AttractionRepository attractionRepository = new AttractionJdbcRepository();
    private final DBConnectionUtil dbConnectionUtil;

    private AttractionJdbcRepository() {
        dbConnectionUtil = DBConnectionUtil.getInstance();
    }

    public static AttractionRepository getAttractionRepository() {
        return attractionRepository;
    }


    @Override
    public List<AttractionInfo> findByConditions(AttractionSearch condition) {
        List<AttractionInfo> attractionInfos = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = dbConnectionUtil.getConnection();
            String sql = "select * from attraction_info" +
                    " where sido_code=? and gugun_code=? and content_type_id=?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, condition.getSidoCode());
            pstmt.setInt(2, condition.getGugunCode());
            pstmt.setInt(3, condition.getContentTypeId());

            rs = pstmt.executeQuery();
            while (rs.next()) {
                attractionInfos.add(createAttractionInfo(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            dbConnectionUtil.close(rs, pstmt, conn);
        }
        return attractionInfos;
    }

    private AttractionInfo createAttractionInfo(ResultSet rs) throws SQLException {
        return AttractionInfo.builder()
                .id(rs.getInt("content_id"))
                .title(rs.getString("title"))
                .addr1(rs.getString("addr1"))
                .addr2(rs.getString("addr2"))
                .zipcode(rs.getString("zipcode"))
                .tel(rs.getString("tel"))
                .firstImage(rs.getString("first_image"))
                .firstImage2(rs.getString("first_image2"))
                .readCount(rs.getInt("readcount"))
                .latitude(rs.getDouble("latitude"))
                .longitude(rs.getDouble("longitude"))
                .mlevel(rs.getString("mlevel"))
                .build();
    }
}