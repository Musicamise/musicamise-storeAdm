package models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alvaro on 19/03/2015.
 */
public class Utils {

    public enum ProductSizeType {
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

    public enum ProductType{
        camiseta("Camiseta"),
        regata("Regata"),
        bata("Bata"),
        manga_longa("Manga Longa"),
        moletom("Moletom");
        String name;
        private ProductType(String name){
            this.name = name;
        }

        public static List<String> getList() {
            List<String> tags = new ArrayList<>();

            for (Utils.ProductType tag : Utils.ProductType.values()) {
                tags.add(tag.name());
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
        specificProduct("Apenas um produto");
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
        masculino("Masculino"),
        feminino("Feminino"),
        infantil("Infantil"),
        posters("Poster"),
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

    public enum UserTags {
        rock,
        pop,
        facebook,
        instagram,
        losHermanos,
        metal,
        heavyMetal;
        public static List<String> getList() {
            List<String> tags = new ArrayList<>();

            for (Utils.UserTags tag : Utils.UserTags.values()) {
                tags.add(tag.name());
            }
            return tags;
        }
    }

    public enum Content {
        frontPage,
        socialPage,
        aboutUsPage,
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
}
