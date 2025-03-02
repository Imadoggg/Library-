package com.library.services;

import com.library.models.BorrowRecord;
import com.library.models.Member;
import java.util.List;

public interface BorrowRecordService {
    /**
     * ดึงประวัติการยืมหนังสือของสมาชิก
     * @param member สมาชิกที่ต้องการดึงประวัติ
     * @return รายการบันทึกการยืม
     */
    List<BorrowRecord> getBorrowHistoryForMember(Member member);

    /**
     * บันทึกการยืมหนังสือ
     * @param borrowRecord บันทึกการยืมที่ต้องการบันทึก
     * @return true หากบันทึกสำเร็จ
     */
    boolean saveBorrowRecord(BorrowRecord borrowRecord);

    /**
     * คืนหนังสือ
     * @param borrowRecord บันทึกการยืมที่ต้องการคืน
     * @return true หากคืนสำเร็จ
     */
    boolean returnBook(BorrowRecord borrowRecord);
}