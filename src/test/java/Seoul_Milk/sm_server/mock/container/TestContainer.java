package Seoul_Milk.sm_server.mock.container;

import Seoul_Milk.sm_server.domain.image.controller.ImageController;
import Seoul_Milk.sm_server.domain.image.repository.ImageRepository;
import Seoul_Milk.sm_server.domain.image.service.ImageServiceImpl;
import Seoul_Milk.sm_server.domain.member.controller.MemberController;
import Seoul_Milk.sm_server.domain.member.repository.MemberRepository;
import Seoul_Milk.sm_server.domain.member.service.MemberServiceImpl;
import Seoul_Milk.sm_server.domain.taxInvoice.controller.TaxInvoiceController;
import Seoul_Milk.sm_server.domain.taxInvoice.repository.TaxInvoiceRepository;
import Seoul_Milk.sm_server.domain.taxInvoice.service.TaxInvoiceServiceImpl;
import Seoul_Milk.sm_server.domain.taxInvoice.util.ExcelMaker;
import Seoul_Milk.sm_server.domain.taxInvoiceFile.repository.TaxInvoiceFileRepository;
import Seoul_Milk.sm_server.global.infrastructure.clovaOcr.infrastructure.ClovaOcrApi;
import Seoul_Milk.sm_server.global.infrastructure.clovaOcr.service.OcrDataExtractor;
import Seoul_Milk.sm_server.global.infrastructure.upload.service.AwsS3Service;
import Seoul_Milk.sm_server.mock.repository.FakeImageRepository;
import Seoul_Milk.sm_server.mock.repository.FakeMemberRepository;
import Seoul_Milk.sm_server.mock.repository.FakeTaxInvoiceFileRepository;
import Seoul_Milk.sm_server.mock.repository.FakeTaxInvoiceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mockito.Mockito;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TestContainer {

    public final MemberRepository memberRepository;
    public final MemberServiceImpl memberService;
    public final MemberController memberController;

    public final TaxInvoiceFileRepository taxInvoiceFileRepository;
    public final TaxInvoiceRepository taxInvoiceRepository;
    public final TaxInvoiceServiceImpl taxInvoiceService;
    public final TaxInvoiceController taxInvoiceController;

    public final ImageRepository imageRepository;
    public final ImageServiceImpl imageService;
    public final ImageController imageController;

    public TestContainer() {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        this.memberRepository = new FakeMemberRepository();
        this.memberService = MemberServiceImpl.builder()
                .memberRepository(this.memberRepository)
                .passwordEncoder(passwordEncoder)
                .build();
        this.memberController = MemberController.builder()
                .memberService(this.memberService)
                .build();


        AwsS3Service awsS3Service = Mockito.mock(AwsS3Service.class);
        ObjectMapper objectMapper = new ObjectMapper();

        this.imageRepository = new FakeImageRepository();
        this.imageService = ImageServiceImpl.builder()
                .awsS3Service(awsS3Service)
                .objectMapper(objectMapper)
                .build();
        this.imageController = ImageController.builder()
                .imageService(this.imageService)
                .build();


        OcrDataExtractor ocrDataExtractor = new OcrDataExtractor();
        ClovaOcrApi clovaOcrApi = Mockito.mock(ClovaOcrApi.class);
        ExcelMaker excelMaker = Mockito.mock(ExcelMaker.class);

        this.taxInvoiceFileRepository = new FakeTaxInvoiceFileRepository();
        this.taxInvoiceRepository = new FakeTaxInvoiceRepository();
        this.taxInvoiceService = new TaxInvoiceServiceImpl(
                clovaOcrApi,
                ocrDataExtractor,
                this.taxInvoiceRepository,
                this.taxInvoiceFileRepository,
                this.imageService,
                awsS3Service,
                excelMaker
        );
        this.taxInvoiceController = new TaxInvoiceController(
                this.taxInvoiceService,
                this.imageService
        );

    }
}
