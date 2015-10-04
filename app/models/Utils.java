package models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alvaro on 19/03/2015.
 */
public class Utils {

    public enum ProductSizeType {
        UNICO("Tamanho único"),
        RN("recem nascido"),
        um("um ano"),
        dois("dois anos"),
        tres("tres anos"),
        quarto("quatro anos"),
        XP("extra pequeno"),
        P("pequeno"), 
        M("medio"),
        G("grande"),
        XG("extra grande"),
        XXG("extra grande"),
        XXXG("extra grande");

        String name;
        private ProductSizeType(String name){
            this.name = name;
        }
        public static List<String> getList() {
            List<String> tags = new ArrayList<>();

            for (Utils.ProductSizeType tag : Utils.ProductSizeType.values()) {
                tags.add(tag.name());
            }
            return tags;
        }
    }

    public enum ProductRecheioType {
        goiaba("Goiaba"), 
        noses("Noses"),
        romeo_e_julieta("Romeo e Julieta"),
        nutella("nutella");

        String name;
        private ProductRecheioType(String name){
            this.name = name;
        }
        public static List<String> getList() {
            List<String> tags = new ArrayList<>();

            for (Utils.ProductRecheioType tag : Utils.ProductRecheioType.values()) {
                tags.add(tag.name());
            }
            return tags;
        }
    }

    public enum ProductType{
        tulipa("Tulipa"),
        hortencias("Hortencias"),
        folhas_em_ramas("Folhas em Ramas");

        String name;
        private ProductType(String name){
            this.name = name;
        }

        public static List<String> getList() {
            List<String> tags = new ArrayList<>();

            for (Utils.ProductType tag : Utils.ProductType.values()) {
                tags.add(tag.name);
            }
            return tags;
        }
    }
    public enum ProductStruct{
        base("Base"),
        flores("Flores");

        String name;
        private ProductType(String name){
            this.name = name;
        }

        public static List<String> getList() {
            List<String> tags = new ArrayList<>();

            for (Utils.ProductType tag : Utils.ProductType.values()) {
                tags.add(tag.name);
            }
            return tags;
        }
    }

    public enum DiscountType {
        value("Value (R$)"),
        percent("Percent( % )");
        String content;
        private DiscountType(String name){
            this.content = name;
        }
        public static List<String> getList() {
            List<String> types = new ArrayList<>();

            for (Utils.DiscountType type : Utils.DiscountType.values()) {
                types.add(type.name());
            }
            return types;
        }
    }

    public enum DiscountValidation {
        all("Todos produtos"),
        overValue("Valor acima"),
        collections("Coleções"),
        specificProduct("Apenas uns produto");
        String content;
        private DiscountValidation(String name){
            this.content = name;
        }
        public static List<String> getList() {
            List<String> types = new ArrayList<>();

            for (Utils.DiscountValidation type : Utils.DiscountValidation.values()) {
                types.add(type.name());
            }
            return types;
        }
    }
    public enum DiscountPaymentAdjust {
        oncePerOrder("Aplicar apenas no compra"),
        toEveryProduct("Aplicar em cada produto");
        String content;
        private DiscountPaymentAdjust(String name){
            this.content = name;
        }
        public static List<String> getList() {
            List<String> types = new ArrayList<>();

            for (Utils.DiscountPaymentAdjust type : Utils.DiscountPaymentAdjust.values()) {
                types.add(type.name());
            }
            return types;
        }
    }

    public enum CollectionType {
        destaque("Destaque"),
        unisex("Unisex"),
        masculino("Masculino"),
        feminino("Feminino"),
        infantil("Infantil"),
        posters("Poster"),
        casa("Casa"),
        poster("Poster"),
        especial("Especial"),
        foraDeEstoque("Fora de Estoque");
        public String title;
        private CollectionType(String title){
            this.title = title;
        }
        public static List<String> getList() {
            List<String> tags = new ArrayList<>();

            for (Utils.CollectionType tag : Utils.CollectionType.values()) {
                tags.add(tag.name());
            }
            return tags;
        }
    }

    public enum GenderUser{
        masculino("Masculino"),
        feminino("Feminino");

        public String title;
        private GenderUser(String title){
            this.title = title;
        }
    }

    public enum UserTags {
        rock("Rock"),
        pop("Pop"),
        facebook("Facebook"),
        instagram("Instagram"),
        losHermanos("Los Hermanos"),
        metal("Metal"),
        heavyMetal("Heavy Metal");
        public String title;
        private UserTags(String title){
            this.title = title;
        }
        public static List<String> getList() {
            List<String> tags = new ArrayList<>();

            for (Utils.UserTags tag : Utils.UserTags.values()) {
                tags.add(tag.name());
            }
            return tags;
        }
    }

    public enum SiteContent {
        frontPage,
        socialPage,
        aboutUsPage,
    }

    public enum DashBoardQueryType {
        gender,
        size,
        inventoryId,
    }



  

/*
    1   Aguardando pagamento: o comprador iniciou a transação, mas até o momento o PagSeguro não recebeu nenhuma informação sobre o pagamento.
    2   Em análise: o comprador optou por pagar com um cartão de crédito e o PagSeguro está analisando o risco da transação.
    3   Paga: a transação foi paga pelo comprador e o PagSeguro já recebeu uma confirmação da instituição financeira responsável pelo processamento.
    4   Disponível: a transação foi paga e chegou ao final de seu prazo de liberação sem ter sido retornada e sem que haja nenhuma disputa aberta.
    5   Em disputa: o comprador, dentro do prazo de liberação da transação, abriu uma disputa.
    6   Devolvida: o valor da transação foi devolvido para o comprador.
    7   Cancelada: a transação foi cancelada sem ter sido finalizada.
    8   Chargeback debitado: o valor da transação foi devolvido para o comprador.
    9   Em contestação: o comprador abriu uma solicitação de chargeback junto à operadora do cartão de crédito.
*/

    public enum StatusCompra{
        AGUARDANDO(1),EMANALISE(2),PAGO(3),DISPONIVEL(4),EMDISPUTA(5),DEVOLVIDA(6),CANCELADO(7),CHARGEBACK(8),EMCONESTACAO(9);
        public final int code;

        StatusCompra(int code){
            this.code = code;
        }

        public static StatusCompra getStatusByCode(int code){
            switch (code){
                case 1: return AGUARDANDO;
                case 2: return EMANALISE;
                case 3: return PAGO;
                case 4: return DISPONIVEL;
                case 5: return EMDISPUTA;
                case 6: return DEVOLVIDA;
                case 7: return CANCELADO;
                case 8: return CHARGEBACK;
                case 9: return EMCONESTACAO;
                default: return null;
            }
        }

    }
     public enum StatusEntrega{
        SEMSTATUS(0),PRODUCAO(1),EMBALAGEM(2),EMTRANSITO(3),ENTREGUE(4);
        public final int code;

        StatusEntrega(int code){
            this.code = code;
        }

        public static StatusEntrega getStatusByCode(int code){
            switch (code){
                case 1: return PRODUCAO;
                case 2: return EMBALAGEM;
                case 3: return EMTRANSITO;
                case 4: return ENTREGUE;
                default: return SEMSTATUS;
            }
        }
        
        public static StatusEntrega getStatusByName(String name){
            switch (name){
                case "PRODUCAO": return PRODUCAO;
                case "EMBALAGEM": return EMBALAGEM;
                case "EMTRANSITO": return EMTRANSITO;
                case "ENTREGUE": return ENTREGUE;
                default: return SEMSTATUS;
            }
        }

    }
    public enum PagseguroTypeCompra{
        PAGAMENTO(1),ASSINATURA(11);
        public final int code;

        PagseguroTypeCompra(int code){
            this.code = code;
        }

        public static PagseguroTypeCompra getTypeByCode(int code){
            switch (code){
                case 1: return PAGAMENTO;
                case 11: return ASSINATURA;
                default: return null;
            }
        }

    }
    public enum PagseguroPagamentoTypeCompra{
        CARTAO_DE_CREDITO(1),BOLETO(2),DEBITO_ONLINE(3),SALDO_PAGSEGURO(4),OI_PAGO(5),DEPOSITO_EM_CONTA(7);
        public final int code;

        PagseguroPagamentoTypeCompra(int code){
            this.code = code;
        }

        public static PagseguroPagamentoTypeCompra getTypeByCode(int code){
            switch (code){
                case 1: return CARTAO_DE_CREDITO;
                case 2: return BOLETO;
                case 3: return DEBITO_ONLINE;
                case 4: return SALDO_PAGSEGURO;
                case 5: return OI_PAGO;
                case 7: return DEPOSITO_EM_CONTA;
                default: return null;
            }
        }

    }
    public enum PagseguroPagamentoCodeCompra{

        CARTAO_DE_CREDITO_VISA(101),
        CARTAO_DE_CREDITO_MASTERCARD(102),
        CARTAO_DE_CREDITO_AMERICAN_EXPRESS(103),
        CARTAO_DE_CREDITO_DINERS(104),
        CARTAO_DE_CREDITO_HIPERCARD(105),
        CARTAO_DE_CREDITO_AURA(106),
        CARTAO_DE_CREDITO_ELO(107),
        CARTAO_DE_CREDITO_PLENOCARD(108),
        CARTAO_DE_CREDITO_PERSONALCARD(109),
        CARTAO_DE_CREDITO_JCB(110),
        CARTAO_DE_CREDITO_DISCOVER(111),
        CARTAO_DE_CREDITO_BRASILCARD(112),
        CARTAO_DE_CREDITO_FORTBRASIL(113),
        CARTAO_DE_CREDITO_CARDBAN(114),
        CARTAO_DE_CREDITO_VALECARD(115),
        CARTAO_DE_CREDITO_CABAL(116),
        CARTAO_DE_CREDITO_MAIS(117),
        CARTAO_DE_CREDITO_AVISTA(118),
        CARTAO_DE_CREDITO_GRANDCARD(119),
        CARTAO_DE_CREDITO_SOROCRED(120),
        BOLETO_BRADESCO(201),
        BOLETO_SANTANDER(202),
        DEBITO_ONLINE_BRADESCO(301),
        DEBITO_ONLINE_ITAU(302),
        DEBITO_ONLINE_UNIBANCO(303),
        DEBITO_ONLINE_BANCO_DO_BRASIL(304),
        DEBITO_ONLINE_BANCO_REAL(305),
        DEBITO_ONLINE_BANRISUL(306),
        DEBITO_ONLINE_HSBC(307),
        SALDO_PAGSEGURO(401),
        OI_PAGGO(501),
        DEPOSITO_EM_CONTA___BANCO_DO_BRASIL(701),
        DEPOSITO_EM_CONTA___HSBC(702);
        public final int code;

        PagseguroPagamentoCodeCompra(int code){
            this.code = code;
        }

        public static PagseguroPagamentoCodeCompra getCompraByCode(int code){
            switch (code){
                case 101: return CARTAO_DE_CREDITO_VISA ;
                case 102: return CARTAO_DE_CREDITO_MASTERCARD ;
                case 103: return CARTAO_DE_CREDITO_AMERICAN_EXPRESS ;
                case 104: return CARTAO_DE_CREDITO_DINERS ;
                case 105: return CARTAO_DE_CREDITO_HIPERCARD ;
                case 106: return CARTAO_DE_CREDITO_AURA ;
                case 107: return CARTAO_DE_CREDITO_ELO ;
                case 108: return CARTAO_DE_CREDITO_PLENOCARD  ;
                case 109: return CARTAO_DE_CREDITO_PERSONALCARD ;
                case 110: return CARTAO_DE_CREDITO_JCB ;
                case 111: return CARTAO_DE_CREDITO_DISCOVER ;
                case 112: return CARTAO_DE_CREDITO_BRASILCARD ;
                case 113: return CARTAO_DE_CREDITO_FORTBRASIL ;
                case 114: return CARTAO_DE_CREDITO_CARDBAN  ;
                case 115: return CARTAO_DE_CREDITO_VALECARD ;
                case 116: return CARTAO_DE_CREDITO_CABAL ;
                case 117: return CARTAO_DE_CREDITO_MAIS;
                case 118: return CARTAO_DE_CREDITO_AVISTA ;
                case 119: return CARTAO_DE_CREDITO_GRANDCARD ;
                case 120: return CARTAO_DE_CREDITO_SOROCRED ;
                case 201: return BOLETO_BRADESCO  ;
                case 202: return BOLETO_SANTANDER ;
                case 301: return DEBITO_ONLINE_BRADESCO ;
                case 302: return DEBITO_ONLINE_ITAU ;
                case 303: return DEBITO_ONLINE_UNIBANCO  ;
                case 304: return DEBITO_ONLINE_BANCO_DO_BRASIL ;
                case 305: return DEBITO_ONLINE_BANCO_REAL  ;
                case 306: return DEBITO_ONLINE_BANRISUL ;
                case 307: return DEBITO_ONLINE_HSBC ;
                case 401: return SALDO_PAGSEGURO ;
                case 501: return OI_PAGGO  ;
                case 701: return DEPOSITO_EM_CONTA___BANCO_DO_BRASIL;
                case 702: return DEPOSITO_EM_CONTA___HSBC;
                default: return null;
            }
        }

    }
}
