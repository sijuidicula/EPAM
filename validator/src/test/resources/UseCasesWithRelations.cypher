MATCH (CG:CropGroup)-[HCC:hasCropClass]->
(CC:CropClass {CropClassName: 'Wheat'})-[HCSC:hasCropSubClass]->
(CSC:CropSubClass)-[CSCHCD:hasCropDescription]->
(CD:CropDescription)-[IAI:isAvailableIn]->
(R:Region)<-[HR:hasRegion]-(C:Country {CountryName: 'Argentina'})
MATCH (CSC)-[HCV:hasCropVariety]->
(CV:CropVariety)-[CVHCD:hasCropDescription]->
(CD)-[HGS:hasGrowthScale]->
(GS:GrowthScale)-[HGSS:hasGrowthScaleStages]->(GSS:GrowthScaleStages)
RETURN CG, CC, CSC, CD, CV, C, R, GS, GSS, HR, IAI, HCC, HCSC, CSCHCD, CVHCD, HCV, HGS, HGSS LIMIT 1

MATCH (CG:CropGroup)-[HCC:hasCropClass]->
(CC:CropClass {CropClassName: 'Wheat'})-[HCSC:hasCropSubClass]->
(CSC:CropSubClass)-[CSCHCD:hasCropDescription]->
(CD:CropDescription)-[IAI:isAvailableIn]->
(R:Region {RegionName: 'All'})<-[HR:hasRegion]-(C:Country {CountryName: 'Argentina'})
MATCH (CSC)-[HCV:hasCropVariety]->
(CV:CropVariety)-[CVHCD:hasCropDescription]->
(CD)-[HGS:hasGrowthScale]->
(GS:GrowthScale)-[HGSS:hasGrowthScaleStages]->(GSS:GrowthScaleStages)
RETURN CG, CC, CSC, CD, CV, C, R, GS, GSS, HR, IAI, HCC, HCSC, CSCHCD, CVHCD, HCV, HGS, HGSS LIMIT 1

MATCH (CG:CropGroup)-[HCC:hasCropClass]->
(CC:CropClass)-[HCSC:hasCropSubClass]->
(CSC:CropSubClass {CropSubClassName: 'Spring barley'})-[CSCHCD:hasCropDescription]->
(CD:CropDescription)-[IAI:isAvailableIn]->
(R:Region)<-[HR:hasRegion]-(C:Country {CountryName: 'Argentina'})
MATCH (CSC)-[HCV:hasCropVariety]->
(CV:CropVariety)-[CVHCD:hasCropDescription]->
(CD)-[HGS:hasGrowthScale]->
(GS:GrowthScale)-[HGSS:hasGrowthScaleStages]->(GSS:GrowthScaleStages)
RETURN CG, CC, CSC, CD, CV, C, R, GS, GSS, HR, IAI, HCC, HCSC, CSCHCD, CVHCD, HCV, HGS, HGSS LIMIT 1

MATCH (CG:CropGroup)-[HCC:hasCropClass]->
(CC:CropClass)-[HCSC:hasCropSubClass]->
(CSC:CropSubClass {CropSubClassName: 'Spring barley'})-[CSCHCD:hasCropDescription]->
(CD:CropDescription)-[IAI:isAvailableIn]->
(R:Region {RegionName: 'All'})<-[HR:hasRegion]-(C:Country {CountryName: 'Argentina'})
MATCH (CSC)-[HCV:hasCropVariety]->
(CV:CropVariety)-[CVHCD:hasCropDescription]->
(CD)-[HGS:hasGrowthScale]->
(GS:GrowthScale)-[HGSS:hasGrowthScaleStages]->(GSS:GrowthScaleStages)
RETURN CG, CC, CSC, CD, CV, C, R, GS, GSS, HR, IAI, HCC, HCSC, CSCHCD, CVHCD, HCV, HGS, HGSS LIMIT 1

MATCH (CG:CropGroup)-[HCC:hasCropClass]->
(CC:CropClass)-[HCSC:hasCropSubClass]->
(CSC:CropSubClass)-[CSCHCD:hasCropDescription]->
(CD:CropDescription)-[IAI:isAvailableIn]->
(R:Region)<-[HR:hasRegion]-(C:Country {CountryName: 'Argentina'})
MATCH (CSC)-[HCV:hasCropVariety]->
(CV:CropVariety {CropVarietyName: 'Tamtam'})-[CVHCD:hasCropDescription]->
(CD)-[HGS:hasGrowthScale]->
(GS:GrowthScale)-[HGSS:hasGrowthScaleStages]->(GSS:GrowthScaleStages)
RETURN CG, CC, CSC, CD, CV, C, R, GS, GSS, HR, IAI, HCC, HCSC, CSCHCD, CVHCD, HCV, HGS, HGSS LIMIT 1

MATCH (CG:CropGroup)-[HCC:hasCropClass]->
(CC:CropClass)-[HCSC:hasCropSubClass]->
(CSC:CropSubClass)-[CSCHCD:hasCropDescription]->
(CD:CropDescription)-[IAI:isAvailableIn]->
(R:Region {RegionName: 'All'})<-[HR:hasRegion]-(C:Country {CountryName: 'Argentina'})
MATCH (CSC)-[HCV:hasCropVariety]->
(CV:CropVariety {CropVarietyName: 'Tamtam'})-[CVHCD:hasCropDescription]->
(CD)-[HGS:hasGrowthScale]->
(GS:GrowthScale)-[HGSS:hasGrowthScaleStages]->(GSS:GrowthScaleStages)
RETURN CG, CC, CSC, CD, CV, C, R, GS, GSS, HR, IAI, HCC, HCSC, CSCHCD, CVHCD, HCV, HGS, HGSS LIMIT 1

MATCH (F:Fertilizers {ProdName: 'Sulfan'})-[IAI:isAvailableIn]->
(R:Region)<-[HR:hasRegion]-(C:Country {CountryName: 'Germany'})
MATCH (F)-[HPN:hasProdNutrient]->
(N:Nutrient)-[HNU:hasNutrientUnit]->
(U:Units)-[HUC:hasUnitConversion]->(UC:UnitConversion)
RETURN C, R, F, N, U, UC, IAI, HR, HPN, HNU, HUC LIMIT 1

MATCH (F:Fertilizers {ProdName: 'Sulfan'})-[IAI:isAvailableIn]->
(R:Region {RegionName: 'All'})<-[HR:hasRegion]-(C:Country {CountryName: 'Germany'})
MATCH (F)-[HPN:hasProdNutrient]->
(N:Nutrient)-[HNU:hasNutrientUnit]->
(U:Units)-[HUC:hasUnitConversion]->(UC:UnitConversion)
RETURN C, R, F, N, U, UC, IAI, HR, HPN, HNU, HUC LIMIT 1

MATCH (CG:CropGroup)-[HCC:hasCropClass]->
(CC:CropClass)-[HCSC:hasCropSubClass]->
(CSC:CropSubClass)-[CSCHCD:hasCropDescription]->
(CD:CropDescription)-[CDIAI:isAvailableIn]->
(R:Region {RegionName: 'All'})<-[HR:hasRegion]-(C:Country {CountryName: 'Canada'})
MATCH (CSC)-[HCV:hasCropVariety]->
(CV:CropVariety {CropVarietyName: 'Zulu'})-[CVHCD:hasCropDescription]->
(CD)-[HGS:hasGrowthScale]->
(GS:GrowthScale)-[HGSS:hasGrowthScaleStages]->(GSS:GrowthScaleStages)
MATCH (F:Fertilizers {ProdName: 'Sulfan'})-[FIAI:isAvailableIn]->(R)
MATCH (F)-[HPN:hasProdNutrient]->
(N:Nutrient)-[HNU:hasNutrientUnit]->
(U:Units)-[HUC:hasUnitConversion]->(UC:UnitConversion)
RETURN CG, CC, CSC, CD, CV, C, R, GS, GSS, F, N, U, UC,
       HCC, HCSC, CSCHCD, CDIAI, FIAI, HR, HCV, CVHCD, HGS, HGSS, HPN, HNU, HUC LIMIT 1

MATCH (CG:CropGroup)-[HCC:hasCropClass]->
(CC:CropClass)-[HCSC:hasCropSubClass]->
(CSC:CropSubClass {CropSubClassName: 'Winter wheat'})-[CSCHCD:hasCropDescription]->
(CD:CropDescription)-[CDIAI:isAvailableIn]->
(R:Region {RegionName: 'All'})<-[HR:hasRegion]-(C:Country {CountryName: 'Canada'})
MATCH (CSC)-[HCV:hasCropVariety]->
(CV:CropVariety)-[CVHCD:hasCropDescription]->
(CD)-[HGS:hasGrowthScale]->
(GS:GrowthScale)-[HGSS:hasGrowthScaleStages]->(GSS:GrowthScaleStages)
MATCH (F:Fertilizers {ProdName: 'Sulfan'})-[FIAI:isAvailableIn]->(R)
MATCH (F)-[HPN:hasProdNutrient]->
(N:Nutrient)-[HNU:hasNutrientUnit]->
(U:Units)-[HUC:hasUnitConversion]->(UC:UnitConversion)
RETURN CG, CC, CSC, CD, CV, C, R, GS, GSS, F, N, U, UC,
       HCC, HCSC, CSCHCD, CDIAI, FIAI, HR, HCV, CVHCD, HGS, HGSS, HPN, HNU, HUC LIMIT 1

MATCH (CG:CropGroup)-[HCC:hasCropClass]->
(CC:CropClass {CropClassName: 'Wheat'})-[HCSC:hasCropSubClass]->
(CSC:CropSubClass)-[CSCHCD:hasCropDescription]->
(CD:CropDescription)-[CDIAI:isAvailableIn]->
(R:Region {RegionName: 'All'})<-[HR:hasRegion]-(C:Country {CountryName: 'Canada'})
MATCH (CSC)-[HCV:hasCropVariety]->
(CV:CropVariety)-[CVHCD:hasCropDescription]->
(CD)-[HGS:hasGrowthScale]->
(GS:GrowthScale)-[HGSS:hasGrowthScaleStages]->(GSS:GrowthScaleStages)
MATCH (F:Fertilizers {ProdName: 'Sulfan'})-[FIAI:isAvailableIn]->(R)
MATCH (F)-[HPN:hasProdNutrient]->
(N:Nutrient)-[HNU:hasNutrientUnit]->
(U:Units)-[HUC:hasUnitConversion]->(UC:UnitConversion)
RETURN CG, CC, CSC, CD, CV, C, R, GS, GSS, F, N, U, UC,
       HCC, HCSC, CSCHCD, CDIAI, FIAI, HR, HCV, CVHCD, HGS, HGSS, HPN, HNU, HUC LIMIT 1

MATCH (CG:CropGroup)-[HCC:hasCropClass]->
(CC:CropClass)-[HCSC:hasCropSubClass]->
(CSC:CropSubClass)-[CSCHCD:hasCropDescription]->
(CD:CropDescription)-[CDIAI:isAvailableIn]->
(R:Region)<-[HR:hasRegion]-(C:Country {CountryName: 'Canada'})
MATCH (CSC)-[HCV:hasCropVariety]->
(CV:CropVariety {CropVarietyName: 'Zulu'})-[CVHCD:hasCropDescription]->
(CD)-[HGS:hasGrowthScale]->
(GS:GrowthScale)-[HGSS:hasGrowthScaleStages]->(GSS:GrowthScaleStages)
MATCH (F:Fertilizers {ProdName: 'Sulfan'})-[FIAI:isAvailableIn]->(R)
MATCH (F)-[HPN:hasProdNutrient]->
(N:Nutrient)-[HNU:hasNutrientUnit]->
(U:Units)-[HUC:hasUnitConversion]->(UC:UnitConversion)
RETURN CG, CC, CSC, CD, CV, C, R, GS, GSS, F, N, U, UC,
       HCC, HCSC, CSCHCD, CDIAI, FIAI, HR, HCV, CVHCD, HGS, HGSS, HPN, HNU, HUC LIMIT 1

MATCH (CG:CropGroup)-[HCC:hasCropClass]->
(CC:CropClass)-[HCSC:hasCropSubClass]->
(CSC:CropSubClass {CropSubClassName: 'Winter wheat'})-[CSCHCD:hasCropDescription]->
(CD:CropDescription)-[CDIAI:isAvailableIn]->
(R:Region)<-[HR:hasRegion]-(C:Country {CountryName: 'Canada'})
MATCH (CSC)-[HCV:hasCropVariety]->
(CV:CropVariety)-[CVHCD:hasCropDescription]->
(CD)-[HGS:hasGrowthScale]->
(GS:GrowthScale)-[HGSS:hasGrowthScaleStages]->(GSS:GrowthScaleStages)
MATCH (F:Fertilizers {ProdName: 'Sulfan'})-[FIAI:isAvailableIn]->(R)
MATCH (F)-[HPN:hasProdNutrient]->
(N:Nutrient)-[HNU:hasNutrientUnit]->
(U:Units)-[HUC:hasUnitConversion]->(UC:UnitConversion)
RETURN CG, CC, CSC, CD, CV, C, R, GS, GSS, F, N, U, UC,
       HCC, HCSC, CSCHCD, CDIAI, FIAI, HR, HCV, CVHCD, HGS, HGSS, HPN, HNU, HUC LIMIT 1

MATCH (CG:CropGroup)-[HCC:hasCropClass]->
(CC:CropClass {CropClassName: 'Wheat'})-[HCSC:hasCropSubClass]->
(CSC:CropSubClass)-[CSCHCD:hasCropDescription]->
(CD:CropDescription)-[CDIAI:isAvailableIn]->
(R:Region)<-[HR:hasRegion]-(C:Country {CountryName: 'Canada'})
MATCH (CSC)-[HCV:hasCropVariety]->
(CV:CropVariety)-[CVHCD:hasCropDescription]->
(CD)-[HGS:hasGrowthScale]->
(GS:GrowthScale)-[HGSS:hasGrowthScaleStages]->(GSS:GrowthScaleStages)
MATCH (F:Fertilizers {ProdName: 'Sulfan'})-[FIAI:isAvailableIn]->(R)
MATCH (F)-[HPN:hasProdNutrient]->
(N:Nutrient)-[HNU:hasNutrientUnit]->
(U:Units)-[HUC:hasUnitConversion]->(UC:UnitConversion)
RETURN CG, CC, CSC, CD, CV, C, R, GS, GSS, F, N, U, UC,
       HCC, HCSC, CSCHCD, CDIAI, FIAI, HR, HCV, CVHCD, HGS, HGSS, HPN, HNU, HUC LIMIT 1
