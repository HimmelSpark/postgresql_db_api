package com.dbapi.adamyan.DAO;

import com.dbapi.adamyan.Model.Vote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;

@Service
@Transactional
public class VoteDAO {
    private final JdbcTemplate jdbc;

    @Autowired
    public VoteDAO(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static VoteMapper voteMapper = new VoteMapper();

    public Vote getVote(String nickname, Integer thread_id) {
        String query = "SELECT * FROM votes WHERE nickname=?::citext AND thread=?";
        try {
            return jdbc.queryForObject(query, voteMapper, nickname, thread_id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public void updateVote(String nickname, Integer thread_id, Integer voice) {
        String sql = "UPDATE votes SET voice=? WHERE thread=? AND nickname=?::citext";
        jdbc.update(sql, voice, thread_id, nickname);
    }

    public static class VoteMapper implements RowMapper<Vote> {
        @Override
        public Vote mapRow(ResultSet result, int i) throws SQLException {
            return new Vote(result.getString("nickname"), result.getInt("thread"), result.getInt("voice"));
        }
    }
}
