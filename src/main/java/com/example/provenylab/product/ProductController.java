package com.example.provenylab.product;
import com.fasterxml.jackson.core.JsonProcessingException; import org.springframework.web.bind.annotation.*; import java.util.Map;
@RestController @RequestMapping("/fake")
public class ProductController {private final ProductMintService service; public ProductController(ProductMintService service){this.service=service;} @PostMapping("/products/mint") public Map<String,Object> mint(@RequestBody MintRequest request)throws JsonProcessingException{return service.mint(request.serialNumber());} @PostMapping("/passports/{passportId}/transfer") public Map<String,Object> transfer(@PathVariable String passportId,@RequestBody Map<String,String> body)throws JsonProcessingException{return service.transfer(passportId,body.getOrDefault("toOwnerId","user-2"));}}
