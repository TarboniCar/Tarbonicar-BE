package com.tarbonicar.backend.api.category.controller;

import com.tarbonicar.backend.api.category.dto.CarAgeResponseDTO;
import com.tarbonicar.backend.api.category.dto.CarNameResponseDTO;
import com.tarbonicar.backend.api.category.dto.CarTypeResponseDTO;
import com.tarbonicar.backend.api.category.dto.CategoryCreateDTO;
import com.tarbonicar.backend.api.category.service.CategoryService;
import com.tarbonicar.backend.common.response.ApiResponse;
import com.tarbonicar.backend.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Category", description = "카테고리 관련 API입니다.")
@RequestMapping("/api/v1/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(
            summary = "카테고리 등록 API", description = "새로운 카테고리를 등록합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "카테고리 등록 성공")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createCategory(@RequestBody CategoryCreateDTO categoryCreateDTO){

        categoryService.createCategory(categoryCreateDTO);
        return ApiResponse.success_only(SuccessStatus.CREATE_CATEGORY_SUCCESS);
    }

    @Operation(summary = "차종 카테고리 조회 API", description = "등록 된 차종 리스트를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "차량 종류 카테고리 조회 성공")
    })
    @GetMapping("/search/cartype")
    public ResponseEntity<ApiResponse<List<CarTypeResponseDTO>>> getCarTypeCategory() {

        List<CarTypeResponseDTO> types = categoryService.getCarTypeCategory();
        return ApiResponse.success(SuccessStatus.SEND_CARTYPE_CATEGORY_SUCCESS, types);
    }

    @Operation(summary = "차량 카테고리 조회 API", description = "등록 된 차량 리스트를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "차량 카테고리 조회 성공")
    })
    @GetMapping("/search/carname")
    public ResponseEntity<ApiResponse<List<CarNameResponseDTO>>> getCarNameCategory(@RequestParam String carType) {

        List<CarNameResponseDTO> names = categoryService.getCarNameCategory(carType);
        return ApiResponse.success(SuccessStatus.SEND_CARNAME_CATEGORY_SUCCESS, names);
    }

    @Operation(summary = "[게시글 작성 페이지 전용] 차량 연식 카테고리 조회 API", description = "등록 된 차량 연식 리스트를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "차량 연식 카테고리 조회 성공")
    })
    @GetMapping("/search/carage")
    public ResponseEntity<ApiResponse<List<CarAgeResponseDTO>>> getCarAgeCategory(@RequestParam String carName) {

        List<CarAgeResponseDTO> ages = categoryService.getCarAgeCategory(carName);
        return ApiResponse.success(SuccessStatus.SEND_CARNAME_CATEGORY_SUCCESS, ages);
    }

    @Operation(
            summary = "[메인, 게시글 리스트 페이지 전용] 차량 연식 카테고리 조회 API",
            description = "조건에 따라 연식 리스트를 조회합니다. <br>" + "<br>만약 모두 전체 보기로 할 때 -> carType = all, carName = all <br>" + "만약 SUV에서 전체 보기로 할 때 -> carType = SUV, carName = all <br>" + "만약 SUV에서 투싼의 연식을 볼 떄 -> carType = SUV, carName = 투싼"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "차량 연식 카테고리 조회 성공")})
    @GetMapping("/search/home/carage")
    public ResponseEntity<ApiResponse<List<CarAgeResponseDTO>>> getHomeCarAgeCategory(@RequestParam String carType, @RequestParam String carName) {

        List<CarAgeResponseDTO> ages = categoryService.getHomeCarAgeCategory(carType.trim(), carName.trim());
        return ApiResponse.success(SuccessStatus.SEND_CARNAME_CATEGORY_SUCCESS, ages);
    }

    @Operation(
            summary = "차량 연식 카테고리 삭제 API",
            description = "주어진 연식 ID를 가진 차량 연식을 삭제합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "차량 연식 카테고리 삭제 성공")
    })
    @DeleteMapping("/carage/{ageId}")
    public ResponseEntity<ApiResponse<Void>> deleteCarAge(
            @PathVariable Long ageId
    ) {
        categoryService.deleteCarAge(ageId);
        return ApiResponse.success_only(SuccessStatus.DELETE_CARAGE_SUCCESS);
    }

    @Operation(
            summary = "차량 이름 카테고리 삭제 API",
            description = "주어진 차량 이름 ID를 가진 카테고리 이름을 삭제합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "차량 이름 카테고리 삭제 성공")
    })
    @DeleteMapping("/carname/{nameId}")
    public ResponseEntity<ApiResponse<Void>> deleteCarName(
            @PathVariable Long nameId
    ) {
        categoryService.deleteCarName(nameId);
        return ApiResponse.success_only(SuccessStatus.DELETE_CARNAME_SUCCESS);
    }

    @Operation(
            summary = "차종 타입 카테고리 삭제 API",
            description = "주어진 차종 타입 ID를 가진 카테고리 타입을 삭제합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "차량 타입 카테고리 삭제 성공")
    })
    @DeleteMapping("/cartype/{typeId}")
    public ResponseEntity<ApiResponse<Void>> deleteCarType(
            @PathVariable Long typeId
    ) {
        categoryService.deleteCarType(typeId);
        return ApiResponse.success_only(SuccessStatus.DELETE_CARTYPE_SUCCESS);
    }

}
