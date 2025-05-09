package Seoul_Milk.sm_server.mock.container;

import Seoul_Milk.sm_server.domain.image.controller.ImageController;
import Seoul_Milk.sm_server.domain.image.repository.ImageRepository;
import Seoul_Milk.sm_server.domain.image.service.ImageServiceImpl;
import Seoul_Milk.sm_server.domain.member.controller.AdminMemberController;
import Seoul_Milk.sm_server.domain.member.controller.MemberController;
import Seoul_Milk.sm_server.domain.member.repository.MemberRepository;
import Seoul_Milk.sm_server.domain.member.service.MemberServiceImpl;
import Seoul_Milk.sm_server.domain.taxInvoice.controller.TaxInvoiceController;
import Seoul_Milk.sm_server.domain.taxInvoice.controller.TaxInvoiceHistoryController;
import Seoul_Milk.sm_server.domain.taxInvoice.controller.TaxInvoiceValidationController;
import Seoul_Milk.sm_server.domain.taxInvoice.repository.TaxInvoiceRepository;
import Seoul_Milk.sm_server.domain.taxInvoice.service.TaxInvoiceHistoryServiceImpl;
import Seoul_Milk.sm_server.domain.taxInvoice.service.TaxInvoiceServiceImpl;
import Seoul_Milk.sm_server.domain.taxInvoice.service.TaxInvoiceValidationServiceImpl;
import Seoul_Milk.sm_server.domain.taxInvoice.thread.RequestThreadFactory;
import Seoul_Milk.sm_server.domain.taxInvoice.util.ExcelMaker;
import Seoul_Milk.sm_server.domain.taxInvoice.validator.TaxInvoiceValidator;
import Seoul_Milk.sm_server.domain.taxInvoiceFile.repository.TaxInvoiceFileRepository;
import Seoul_Milk.sm_server.global.infrastructure.clovaOcr.infrastructure.ClovaOcrApi;
import Seoul_Milk.sm_server.global.infrastructure.clovaOcr.service.OcrDataExtractor;
import Seoul_Milk.sm_server.global.infrastructure.codef.CodefFactory;
import Seoul_Milk.sm_server.global.infrastructure.redis.RedisUtils;
import Seoul_Milk.sm_server.global.infrastructure.upload.service.AwsS3Service;
import Seoul_Milk.sm_server.mock.repository.FakeImageRepository;
import Seoul_Milk.sm_server.mock.repository.FakeMemberRepository;
import Seoul_Milk.sm_server.mock.repository.FakeTaxInvoiceFileRepository;
import Seoul_Milk.sm_server.mock.repository.FakeTaxInvoiceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mockito.Mockito;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TestContainer {

    public final AwsS3Service awsS3Service;
    public final ClovaOcrApi clovaOcrApi;
    public final ExcelMaker excelMaker;
    public final RedisUtils redisUtils;
    public final RequestThreadFactory requestThreadFactory;
    public final CodefFactory codefFactory;

    public final MemberRepository memberRepository;
    public final MemberServiceImpl memberService;
    public final MemberController memberController;
    public final AdminMemberController adminMemberController;

    public final TaxInvoiceFileRepository taxInvoiceFileRepository;
    public final TaxInvoiceRepository taxInvoiceRepository;
    public final TaxInvoiceServiceImpl taxInvoiceService;
    public final TaxInvoiceController taxInvoiceController;
    public final TaxInvoiceHistoryServiceImpl taxInvoiceHistoryService;
    public final TaxInvoiceHistoryController taxInvoiceHistoryController;
    public final TaxInvoiceValidationServiceImpl taxInvoiceValidationService;
    public final TaxInvoiceValidationController taxInvoiceValidationController;
    public final TaxInvoiceValidator taxInvoiceValidator;

    public final ImageRepository imageRepository;
    public final ImageServiceImpl imageService;
    public final ImageController imageController;

    public TestContainer() {
        this.awsS3Service = Mockito.mock(AwsS3Service.class);
        this.clovaOcrApi = Mockito.mock(ClovaOcrApi.class);
        this.excelMaker = Mockito.mock(ExcelMaker.class);
        this.redisUtils = Mockito.mock(RedisUtils.class);
        this.codefFactory = Mockito.mock(CodefFactory.class);
        this.requestThreadFactory = Mockito.mock(RequestThreadFactory.class);

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        this.memberRepository = new FakeMemberRepository();
        this.memberService = MemberServiceImpl.builder()
                .memberRepository(this.memberRepository)
                .passwordEncoder(passwordEncoder)
                .build();
        this.memberController = MemberController.builder()
                .memberService(this.memberService)
                .build();
        this.adminMemberController = AdminMemberController.builder()
                .memberService(this.memberService)
                .build();

        ObjectMapper objectMapper = new ObjectMapper();

        this.imageRepository = new FakeImageRepository();
        this.imageService = ImageServiceImpl.builder()
                .imageRepository(imageRepository)
                .awsS3Service(awsS3Service)
                .objectMapper(objectMapper)
                .build();
        this.imageController = ImageController.builder()
                .imageService(this.imageService)
                .build();


        OcrDataExtractor ocrDataExtractor = new OcrDataExtractor();

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

        this.taxInvoiceValidator = new TaxInvoiceValidator();
        this.taxInvoiceHistoryService = new TaxInvoiceHistoryServiceImpl(
                this.taxInvoiceRepository,
                this.taxInvoiceValidator
        );
        this.taxInvoiceHistoryController = new TaxInvoiceHistoryController(
                this.taxInvoiceHistoryService
        );

        this.taxInvoiceValidationService = TaxInvoiceValidationServiceImpl
                .builder()
                .redisUtils(redisUtils)
                .codefFactory(codefFactory)
                .PRODUCT_URL("test")
                .taxInvoiceRepository(taxInvoiceRepository)
                .requestThreadFactory(requestThreadFactory)
                .build();
        this.taxInvoiceValidationController = TaxInvoiceValidationController
                .builder()
                .taxInvoiceValidationService(taxInvoiceValidationService)
                .build();
    }
}
