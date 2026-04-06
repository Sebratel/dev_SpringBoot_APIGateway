package br.com.sebratel.bff.cucumber;

import br.com.sebratel.bff.service.*;
import br.com.sebratel.bff.service.comercial.PrimeiroPaganteMensalService;
import br.com.sebratel.bff.service.massivas.*;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import br.com.sebratel.bff.controller.*;

@CucumberContextConfiguration
@WebMvcTest({
    AcquisitionController.class,
    ActiveSellersController.class,
    AffectedUserController.class,
    AuthenticationSitesController.class,
    BlockedContractController.class,
    ConsumptionController.class,
    ContractActivationController.class,
    ContractActivationInvoiceController.class,
    ContractPaymentController.class,
    ContractWithoutInvoiceController.class,
    DuplicateCallingStationController.class,
    DuplicateClientNameReportController.class,
    DuplicatePrefixController.class,
    EmployeeController.class,
    FirstAuthenticationController.class,
    FirstMonthlyPayerController.class,
    InventoryController.class,
    InventoryMovesController.class,
    LastConnectionController.class,
    MassiveElevenController.class,
    MatrixController.class,
    PendingAssetController.class,
    QrCodeController.class,
    SplittersController.class,
    WeeklyReportController.class
})
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
public class CucumberSpringConfiguration {

    @MockitoBean
    private AquisicaoService aquisicaoService;

    @MockitoBean
    private VendedoresAtivosService vendedoresAtivosService;

    @MockitoBean
    private UsuarioAfetadoService usuarioAfetadoService;

    @MockitoBean
    private AuthenticationSitesService authenticationSitesService;

    @MockitoBean
    private ContratoBloqueadoService contratoBloqueadoService;

    @MockitoBean
    private ConsumoService consumoService;

    @MockitoBean
    private ContratoAtivacaoFaturaService contratoAtivacaoFaturaService;

    @MockitoBean
    private ContratoSemFaturaService contratoSemFaturaService;

    @MockitoBean
    private DuplicateCallingStationService duplicateCallingStationService;

    @MockitoBean
    private RelatorioClienteNomeDuplicadoService relatorioClienteNomeDuplicadoService;

    @MockitoBean
    private DuplicatePrefixService duplicatePrefixService;

    @MockitoBean
    private EmployeeService employeeService;

    @MockitoBean
    private FirstAuthenticationService firstAuthenticationService;

    @MockitoBean
    private InventoryService inventoryService;

    @MockitoBean
    private InventoryMovesService inventoryMovesService;

    @MockitoBean
    private UltimaConexaoService ultimaConexaoService;

    @MockitoBean
    private MatrixService matrixService;

    @MockitoBean
    private PatrimonioPendenteService patrimonioPendenteService;

    @MockitoBean
    private QrCodeService qrCodeService;

    @MockitoBean
    private InventoryDataProvider inventoryDataProvider;

    @MockitoBean
    private GetConnectionsService getConnectionsService;

    @MockitoBean
    private ListarSplittersService listarSplittersService;

    @MockitoBean
    private ListarOltsService listarOltsService;

    @MockitoBean
    private RecuperarSolicitacoesDeUmUsuarioService recuperarSolicitacoesDeUmUsuarioService;

    @MockitoBean
    private RecuperarTokenDoUsuarioIntegradorEllevenService recuperarTokenDoUsuarioIntegradorEllevenService;

    @MockitoBean
    private WeeklyReportService weeklyReportService;

    @MockitoBean
    private ContractActivationService contractActivationService;

    @MockitoBean
    private ContractPaymentService contractPaymentService;

    @MockitoBean
    private PrimeiroPaganteMensalService primeiroPaganteMensalService;

    @MockitoBean
    private AdicionarMassivaNoEllevenService adicionarMassivaNoEllevenService;

    @MockitoBean
    private AdicionarMassivaNoEllevenApiService adicionarMassivaNoEllevenApiService;

    @MockitoBean
    private EnviarListaDeAfetadosParaNativeService enviarListaDeAfetadosParaNativeService;

    @MockitoBean
    private GetAllMassivesService getAllMassivesService;

    @MockitoBean
    private RecuperarTodasAsMassivasPeloBancoService recuperarTodasAsMassivasPeloBancoService;

    @MockitoBean
    private FinalizarMassivaNoEllevenApiService finalizarMassivaNoEllevenApiService;
}
