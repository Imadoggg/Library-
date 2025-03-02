package com.library.services;

import com.library.LibraryDataManager;
import com.library.models.Book;
import com.library.models.BorrowRecord;
import com.library.models.Member;
import java.util.List;
import java.util.stream.Collectors;

public class InMemoryBorrowRecordService implements BorrowRecordService {
    private LibraryDataManager dataManager;

    public InMemoryBorrowRecordService() {
        this.dataManager = LibraryDataManager.getInstance();
    }

    @Override
    public List<BorrowRecord> getBorrowHistoryForMember(Member member) {
        // กรองเฉพาะบันทึกการยืมของสมาชิกคนนี้
        return dataManager.getAllBorrowRecords().stream()
                .filter(record -> record.getMember().getId().equals(member.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean saveBorrowRecord(BorrowRecord borrowRecord) {
        // เพิ่มบันทึกการยืม
        return dataManager.getAllBorrowRecords().add(borrowRecord);
    }

    @Override
    public boolean returnBook(BorrowRecord borrowRecord) {
        // ค้นหาและอัปเดตบันทึกการยืมเพื่อคืนหนังสือ
        for (BorrowRecord record : dataManager.getAllBorrowRecords()) {
            if (record.getId().equals(borrowRecord.getId())) {
                record.returnBook();
                return true;
            }
        }
        return false;
    }

    // เมธอดเพิ่มเติมเพื่อความยืดหยุ่น
    public void clearAllRecords() {
        dataManager.getAllBorrowRecords().clear();
    }

    public int getTotalBorrowRecords() {
        return dataManager.getAllBorrowRecords().size();
    }
}