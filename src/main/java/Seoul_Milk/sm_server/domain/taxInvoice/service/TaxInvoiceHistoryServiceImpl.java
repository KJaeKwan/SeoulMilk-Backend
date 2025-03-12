package Seoul_Milk.sm_server.domain.taxInvoice.service;

import static Seoul_Milk.sm_server.domain.taxInvoice.enums.ProcessStatus.APPROVED;
import static Seoul_Milk.sm_server.domain.taxInvoice.enums.ProcessStatus.REJECTED;
import static Seoul_Milk.sm_server.domain.taxInvoice.enums.ProcessStatus.UNAPPROVED;
import static Seoul_Milk.sm_server.global.exception.ErrorCode.DO_NOT_ACCESS_OTHER_TAX_INVOICE;
import static Seoul_Milk.sm_server.global.exception.ErrorCode.TAX_INVOICE_NOT_EXIST;

import Seoul_Milk.sm_server.domain.taxInvoice.entity.TaxInvoice;
import Seoul_Milk.sm_server.domain.taxInvoice.enums.ProcessStatus;
import Seoul_Milk.sm_server.domain.taxInvoice.repository.TaxInvoiceRepository;
import Seoul_Milk.sm_server.domain.taxInvoice.dto.history.TaxInvoiceHistoryResponseDTO;
import Seoul_Milk.sm_server.domain.taxInvoice.dto.history.TaxInvoiceHistoryResponseDTO.GetHistoryData;
import Seoul_Milk.sm_server.domain.taxInvoice.dto.history.TaxInvoiceHistoryResponseDTO.GetModalResponse;
import Seoul_Milk.sm_server.domain.taxInvoice.dto.history.TaxInvoiceHistoryRequestDTO.ChangeTaxInvoiceRequest;
import Seoul_Milk.sm_server.domain.taxInvoice.dto.history.TaxInvoiceHistoryRequestDTO.TaxInvoiceRequest;
import Seoul_Milk.sm_server.domain.taxInvoice.validator.TaxInvoiceValidator;
import Seoul_Milk.sm_server.global.exception.CustomException;
import Seoul_Milk.sm_server.domain.member.entity.MemberEntity;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TaxInvoiceHistoryServiceImpl implements TaxInvoiceHistoryService {
    private final TaxInvoiceRepository taxInvoiceRepository;
    private final TaxInvoiceValidator taxInvoiceValidator;

    @Override
    public TaxInvoiceHistoryResponseDTO.TaxInvoiceSearchResult searchByProviderOrConsumer(MemberEntity memberEntity, ProcessStatus processStatus, String poc, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<TaxInvoice> taxInvoicePage = taxInvoiceRepository.searchConsumerOrProvider(poc, memberEntity.getEmployeeId(), processStatus, memberEntity, pageable);
        Long total = taxInvoiceRepository.getProcessStatusCount(null, memberEntity);
        Long approved = taxInvoiceRepository.getProcessStatusCount(APPROVED, memberEntity);
        Long rejected = taxInvoiceRepository.getProcessStatusCount(REJECTED, memberEntity);
        Long unapproved = taxInvoiceRepository.getProcessStatusCount(UNAPPROVED, memberEntity);

        List<Long> taxInvoiceIds = taxInvoicePage.getContent().stream()
                .map(TaxInvoice::getTaxInvoiceId)
                .toList();
        //임시저장 상태가 INITIAL인건 모두 Untemp로 바꾸기
        taxInvoiceRepository.updateInitialToUntemp(taxInvoiceIds);

        List<GetHistoryData> historyDataList = taxInvoicePage.stream()
                .map(taxInvoice -> TaxInvoiceHistoryResponseDTO.GetHistoryData.from(taxInvoice, taxInvoice.getFile()))
                .toList();

        Page<GetHistoryData> pageGetHistoryData = new PageImpl<>(historyDataList, pageable, taxInvoicePage.getTotalElements());
        return TaxInvoiceHistoryResponseDTO.TaxInvoiceSearchResult.from(pageGetHistoryData, total, approved, rejected, unapproved);
    }

    @Transactional
    @Override
    public Void deleteValidationTaxInvoice(MemberEntity memberEntity, TaxInvoiceRequest taxInvoiceRequest) {
        List<Long> taxInvoiceIdList = taxInvoiceRequest.taxInvoiceIdList();
        List<TaxInvoice> taxInvoices = taxInvoiceRepository.findAllById(taxInvoiceIdList);

        // 검증 수행 (Validator에서 처리)
        taxInvoiceValidator.validateExistence(taxInvoices, taxInvoiceIdList);
        taxInvoiceValidator.validateOwnership(memberEntity, taxInvoices);

        // 한 번의 deleteAll() 호출로 일괄 삭제
        taxInvoiceRepository.deleteAll(taxInvoices);
        return null;
    }

    /**
     * 임시저장 로직
     * @param memberEntity 로그인 유저 정보
     * @param taxInvoiceRequest 임시저장 할 세금계산서 ID 값 리스트
     */
    @Transactional
    @Override
    public Void tempSave(MemberEntity memberEntity, TaxInvoiceRequest taxInvoiceRequest) {
        List<Long> taxInvoiceIdList = taxInvoiceRequest.taxInvoiceIdList();
        List<TaxInvoice> taxInvoices = taxInvoiceRepository.findAllById(taxInvoiceIdList);

        // 검증 수행 (Validator에서 처리)
        taxInvoiceValidator.validateExistence(taxInvoices, taxInvoiceIdList);
        taxInvoiceValidator.validateOwnership(memberEntity, taxInvoices);

        taxInvoiceRepository.updateIsTemporaryToTemp(taxInvoiceIdList);
        return null;
    }

    /**
     * 모달 띄우는 api
     * @param taxInvoiceId 조회할 세금계산서 ID
     * @return 해당 세금계산서에 대한 상세 정보 반환
     */
    @Override
    public GetModalResponse showModal(Long taxInvoiceId) {
        TaxInvoice taxInvoice = taxInvoiceRepository.findById(taxInvoiceId)
                .orElseThrow(() -> new CustomException(TAX_INVOICE_NOT_EXIST));
        return TaxInvoiceHistoryResponseDTO.GetModalResponse.from(taxInvoice);
    }

    /**
     * 수정 api
     * @param memberEntity 로그인 유저 정보
     * @param changeTaxInvoiceRequest 수정할 세금계산서 ID와 수정할 데이터(승인번호, 작성일자, 공급자 등록번호, 공급받는자 등록번호, 공급가액) 입력
     */
    @Override
    public Void changeColunm(MemberEntity memberEntity,
            ChangeTaxInvoiceRequest changeTaxInvoiceRequest) {
        if(!taxInvoiceRepository.isAccessYourTaxInvoice(memberEntity, changeTaxInvoiceRequest.taxInvoiceId())){
            throw new CustomException(DO_NOT_ACCESS_OTHER_TAX_INVOICE);
        }
        taxInvoiceRepository.updateMandatoryColumns(
                changeTaxInvoiceRequest.taxInvoiceId(),
                changeTaxInvoiceRequest.issueId(),
                changeTaxInvoiceRequest.erDat(),
                changeTaxInvoiceRequest.ipId(),
                changeTaxInvoiceRequest.suId(),
                changeTaxInvoiceRequest.chargeTotal()
        );
        return null;
    }
}
