package com.library.dao;

import com.library.models.Member;
import com.library.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MemberDAO {

    public List<Member> getAllMembers() {
        List<Member> members = new ArrayList<>();
        String query = "SELECT id, name, contact FROM members";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String id = rs.getString("id");
                String name = rs.getString("name");
                String contact = rs.getString("contact");

                Member member = new Member(id, name, contact);
                members.add(member);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching members: " + e.getMessage());
            e.printStackTrace();
        }

        return members;
    }

    public Member getMemberById(String id) {
        String query = "SELECT id, name, contact FROM members WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String name = rs.getString("name");
                    String contact = rs.getString("contact");

                    return new Member(id, name, contact);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching member by ID: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public boolean addMember(Member member) {
        String query = "INSERT INTO members (name, contact) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, member.getName());
            pstmt.setString(2, member.getContact());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error adding member: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public String generateNewMemberId() {
        String query = "SELECT MAX(SUBSTRING(id, 2)) AS max_id FROM members WHERE id LIKE 'M%'";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                String maxIdStr = rs.getString("max_id");
                if (maxIdStr != null) {
                    int maxId = Integer.parseInt(maxIdStr);
                    return "M" + String.format("%03d", maxId + 1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error generating new member ID: " + e.getMessage());
            e.printStackTrace();
        }

        return "M001"; // Default if no members exist yet
    }
}