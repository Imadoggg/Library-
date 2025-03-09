package com.library.services;

import com.library.models.BorrowRecord;
import com.library.models.Member;
import java.util.List;

public interface BorrowRecordService {

    List<BorrowRecord> getBorrowHistoryForMember(Member member);

    boolean saveBorrowRecord(BorrowRecord borrowRecord);

    boolean returnBook(BorrowRecord borrowRecord);
}