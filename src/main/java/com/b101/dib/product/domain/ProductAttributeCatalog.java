package com.b101.dib.product.domain;

import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class ProductAttributeCatalog {
    private ProductAttributeCatalog() { }

    private static ProductAttributeSpec text(String key, String label, String placeholder) {
        return new ProductAttributeSpec(key, label, "TEXT", false, placeholder);
    }

    private static ProductAttributeSpec year(String key, String label) {
        return new ProductAttributeSpec(key, label, "YEAR", false, "예: 2024");
    }

    private static ProductAttributeSpec expiry(String label) {
        return new ProductAttributeSpec("expiryDate", label, "DATE", true, "YYYY-MM-DD");
    }

    private static ProductAttributeSpec confirmation(String key, String label) {
        return new ProductAttributeSpec(key, label, "CONFIRM", true, "해당하는 경우 선택해주세요");
    }

    public static List<ProductAttributeSpec> forCategory(String categoryName) {
        return switch (categoryName) {
            case "디지털", "생활가전" -> List.of(text("brand", "브랜드", "예: 삼성"), text("model", "모델명", "예: Galaxy S24"), year("releaseYear", "출시연도"));
            case "가구/인테리어", "생활/주방" -> List.of(text("dimensions", "크기", "가로 × 세로 × 높이"), text("material", "소재", "예: 원목"));
            case "유아동" -> List.of(text("brand", "브랜드", "예: 브랜드명"), text("size", "사이즈·권장 연령", "예: 100호 / 3~4세"));
            case "유아도서", "도서" -> List.of(text("author", "저자", "예: 저자명"), text("isbn", "ISBN", "책 뒷면의 13자리 번호"));
            case "여성의류", "남성패션" -> List.of(text("brand", "브랜드", "예: 브랜드명"), text("size", "사이즈", "예: M / 95"));
            case "여성잡화", "남성잡화" -> List.of(text("brand", "브랜드", "예: 브랜드명"), text("size", "사이즈·규격", "예: 240mm"));
            case "뷰티/미용" -> List.of(text("brand", "브랜드", "예: 브랜드명"), text("volume", "용량", "예: 50ml"),
                    new ProductAttributeSpec("expiryDate", "사용기한", "DATE", false, "YYYY-MM-DD"));
            case "스포츠/레저" -> List.of(text("brand", "브랜드", "예: 브랜드명"), text("size", "사이즈·규격", "예: 270mm"));
            case "취미/게임/음반" -> List.of(text("productNumber", "제품 번호", "예: 레고 10326"),
                    text("platform", "기종·플랫폼", "예: Nintendo Switch"), text("artist", "아티스트", "음반이라면 입력"));
            case "티켓/교환권", "e쿠폰" -> List.of(expiry("유효기간"), confirmation("unused", "미사용 상품입니다"));
            case "가공식품", "건강기능식품" -> List.of(expiry("소비기한"), confirmation("unopened", "미개봉 상품입니다"));
            case "반려동물용품" -> List.of(text("brand", "브랜드", "예: 브랜드명"), text("size", "크기·용량", "예: 2kg"));
            case "식물" -> List.of(text("species", "식물 종류", "예: 몬스테라"), text("size", "크기", "예: 높이 30cm"));
            case "기타" -> List.of(text("additionalInfo", "추가 정보", "규격·재질 등 상품의 특징"));
            default -> List.of();
        };
    }

    public static Map<String, String> validate(String categoryName, Map<String, String> submitted) {
        if (submitted == null) submitted = Map.of();
        List<ProductAttributeSpec> specs = forCategory(categoryName);
        Map<String, ProductAttributeSpec> byKey = specs.stream().collect(Collectors.toMap(ProductAttributeSpec::key, spec -> spec));
        if (submitted.size() > specs.size() || !byKey.keySet().containsAll(submitted.keySet())) {
            throw new BusinessException(ErrorCode.INVALID_PRODUCT_ATTRIBUTES);
        }
        Map<String, String> cleaned = new HashMap<>();
        for (var entry : submitted.entrySet()) {
            String value = entry.getValue() == null ? "" : entry.getValue().trim();
            ProductAttributeSpec spec = byKey.get(entry.getKey());
            if (value.length() > 100) throw new BusinessException(ErrorCode.INVALID_PRODUCT_ATTRIBUTES);
            if (value.isEmpty()) continue;
            try {
                switch (spec.type()) {
                    case "YEAR" -> {
                        int year = Integer.parseInt(value);
                        if (year < 1900 || year > 2100) throw new BusinessException(ErrorCode.INVALID_PRODUCT_ATTRIBUTES);
                    }
                    case "DATE" -> {
                        if (LocalDate.parse(value).isBefore(LocalDate.now())) throw new BusinessException(ErrorCode.INVALID_PRODUCT_ATTRIBUTES);
                    }
                    case "CONFIRM" -> {
                        if (!"true".equals(value)) throw new BusinessException(ErrorCode.INVALID_PRODUCT_ATTRIBUTES);
                    }
                    default -> { }
                }
            } catch (NumberFormatException | DateTimeParseException exception) {
                throw new BusinessException(ErrorCode.INVALID_PRODUCT_ATTRIBUTES);
            }
            cleaned.put(entry.getKey(), value);
        }
        Set<String> missing = specs.stream().filter(ProductAttributeSpec::required).map(ProductAttributeSpec::key)
                .filter(key -> !cleaned.containsKey(key)).collect(Collectors.toSet());
        if (!missing.isEmpty()) throw new BusinessException(ErrorCode.REQUIRED_PRODUCT_ATTRIBUTES_MISSING);
        return Map.copyOf(cleaned);
    }
}
