MATCH (CG:CropGroup)-[:hasCropClass]->
(CC:CropClass {CropClassName: "Wheat"})-[:hasCropSubClass]->
(CSC:CropSubClass)-[:hasCropDescription]->
(CD:CropDescription )-[:isAvailableIn]->
(R:Region)<-[:hasRegion]-(C:Country {CountryName: "Argentina"})
MATCH (CSC)-[:hasCropVariety]->
(CV:CropVariety)-[:hasCropDescription]->
(CD)-[:hasGrowthScale] ->
(GS:GrowthScale)-[:hasGrowthScaleStages]->(GSS:GrowthScaleStages)
RETURN CG,CC,CSC,CD,CV,C,R,GS,GSS

MATCH (CG:CropGroup)-[:hasCropClass]->
(CC:CropClass {CropClassName: "Wheat"})-[:hasCropSubClass]->
(CSC:CropSubClass)-[:hasCropDescription]->
(CD:CropDescription )-[:isAvailableIn]->
(R:Region {RegionName: "All"})<-[:hasRegion]-(C:Country {CountryName: "Argentina"})
MATCH (CSC)-[:hasCropVariety]->
(CV:CropVariety)-[:hasCropDescription]->
(CD)-[:hasGrowthScale] ->
(GS:GrowthScale)-[:hasGrowthScaleStages]->(GSS:GrowthScaleStages)
RETURN CG,CC,CSC,CD,CV,C,R,GS,GSS

MATCH (CG:CropGroup)-[:hasCropClass]->
(CC:CropClass)-[:hasCropSubClass]->
(CSC:CropSubClass {CropSubClassName: "Spring barley"})-[:hasCropDescription]->
(CD:CropDescription)-[:isAvailableIn]->
(R:Region)<-[:hasRegion]-(C:Country {CountryName: "Argentina"})
MATCH (CSC)-[:hasCropVariety]->
(CV:CropVariety)-[:hasCropDescription]->
(CD)-[:hasGrowthScale] ->
(GS:GrowthScale)-[:hasGrowthScaleStages]->(GSS:GrowthScaleStages)
RETURN CG,CC,CSC,CD,CV,C,R,GS,GSS

MATCH (CG:CropGroup)-[:hasCropClass]->
(CC:CropClass)-[:hasCropSubClass]->
(CSC:CropSubClass {CropSubClassName: "Spring barley"})-[:hasCropDescription]->
(CD:CropDescription )-[:isAvailableIn]->
(R:Region {RegionName: "All"})<-[:hasRegion]-(C:Country {CountryName: "Argentina"})
MATCH (CSC)-[:hasCropVariety]->
(CV:CropVariety)-[:hasCropDescription]->
(CD)-[:hasGrowthScale] ->
(GS:GrowthScale)-[:hasGrowthScaleStages]->(GSS:GrowthScaleStages)
RETURN CG,CC,CSC,CD,CV,C,R,GS,GSS

MATCH (CG:CropGroup)-[:hasCropClass]->
(CC:CropClass)-[:hasCropSubClass]->
(CSC:CropSubClass)-[:hasCropDescription]->
(CD:CropDescription )-[:isAvailableIn]->
(R:Region)<-[:hasRegion]-(C:Country {CountryName: "Argentina"})
MATCH (CSC)-[:hasCropVariety]->
(CV:CropVariety {CropVarietyName: "Tamtam"})-[:hasCropDescription]->
(CD)-[:hasGrowthScale] ->
(GS:GrowthScale)-[:hasGrowthScaleStages]->(GSS:GrowthScaleStages)
RETURN CG,CC,CSC,CD,CV,C,R,GS,GSS

MATCH (CG:CropGroup)-[:hasCropClass]->
(CC:CropClass)-[:hasCropSubClass]->
(CSC:CropSubClass)-[:hasCropDescription]->
(CD:CropDescription )-[:isAvailableIn]->
(R:Region {RegionName: "All"})<-[:hasRegion]-(C:Country {CountryName: "Argentina"})
MATCH (CSC)-[:hasCropVariety]->
(CV:CropVariety {CropVarietyName: "Tamtam"})-[:hasCropDescription]->
(CD)-[:hasGrowthScale] ->
(GS:GrowthScale)-[:hasGrowthScaleStages]->(GSS:GrowthScaleStages)
RETURN CG,CC,CSC,CD,CV,C,R,GS,GSS

MATCH (F:Fertilizers {ProdName: "Sulfan"})-[:isAvailableIn]->
(R:Region)<-[:hasRegion]-(C:Country {CountryName: "Germany"})
MATCH (F)-[:hasProdNutrient]->
(N:Nutrient)-[:hasNutrientUnit]->
(U:Units)-[:hasUnitConversion]->(UC:UnitConversion)
RETURN C,R,F,N,U,UC

MATCH (F:Fertilizers {ProdName: "Sulfan"})-[:isAvailableIn]->
(R:Region {RegionName: "All"})<-[:hasRegion]-(C:Country {CountryName: "Germany"})
MATCH (F)-[:hasProdNutrient]->
(N:Nutrient)-[:hasNutrientUnit]->
(U:Units)-[:hasUnitConversion]->(UC:UnitConversion)
RETURN C,R,F,N,U,UC

MATCH (CG:CropGroup)-[:hasCropClass]->
(CC:CropClass)-[:hasCropSubClass]->
(CSC:CropSubClass)-[:hasCropDescription]->
(CD:CropDescription)-[:isAvailableIn]->
(R:Region {RegionName: "All"})<-[:hasRegion]-(C:Country {CountryName: "Canada"})
MATCH (CSC)-[:hasCropVariety]->
(CV:CropVariety {CropVarietyName: "Zulu"})-[:hasCropDescription]->
(CD)-[:hasGrowthScale] ->
(GS:GrowthScale)-[:hasGrowthScaleStages]->(GSS:GrowthScaleStages)
MATCH (F:Fertilizers {ProdName: "Sulfan"})-[:isAvailableIn]->(R)
MATCH (F)-[:hasProdNutrient]->
(N:Nutrient)-[:hasNutrientUnit]->
(U:Units)-[:hasUnitConversion]->(UC:UnitConversion)
RETURN CG,CC,CSC,CD,CV,C,R,GS,GSS,F,N,U,UC

MATCH (CG:CropGroup)-[:hasCropClass]->
(CC:CropClass)-[:hasCropSubClass]->
(CSC:CropSubClass {CropSubClassName: "Winter wheat"})-[:hasCropDescription]->
(CD:CropDescription)-[:isAvailableIn]->
(R:Region {RegionName: "All"})<-[:hasRegion]-(C:Country {CountryName: "Canada"})
MATCH (CSC)-[:hasCropVariety]->
(CV:CropVariety)-[:hasCropDescription]->
(CD)-[:hasGrowthScale] ->
(GS:GrowthScale)-[:hasGrowthScaleStages]->(GSS:GrowthScaleStages)
MATCH (F:Fertilizers {ProdName: "Sulfan"})-[:isAvailableIn]->(R)
MATCH (F)-[:hasProdNutrient]->
(N:Nutrient)-[:hasNutrientUnit]->
(U:Units)-[:hasUnitConversion]->(UC:UnitConversion)
RETURN CG,CC,CSC,CD,CV,C,R,GS,GSS,F,N,U,UC LIMIT 300

MATCH (CG:CropGroup)-[:hasCropClass]->
(CC:CropClass {CropClassName: "Wheat"})-[:hasCropSubClass]->
(CSC:CropSubClass)-[:hasCropDescription]->
(CD:CropDescription)-[:isAvailableIn]->
(R:Region {RegionName: "All"})<-[:hasRegion]-(C:Country {CountryName: "Canada"})
MATCH (CSC)-[:hasCropVariety]->
(CV:CropVariety)-[:hasCropDescription]->
(CD)-[:hasGrowthScale] ->
(GS:GrowthScale)-[:hasGrowthScaleStages]->(GSS:GrowthScaleStages)
MATCH (F:Fertilizers {ProdName: "Sulfan"})-[:isAvailableIn]->(R)
MATCH (F)-[:hasProdNutrient]->
(N:Nutrient)-[:hasNutrientUnit]->
(U:Units)-[:hasUnitConversion]->(UC:UnitConversion)
RETURN CG,CC,CSC,CD,CV,C,R,GS,GSS,F,N,U,UC LIMIT 300

MATCH (CG:CropGroup)-[:hasCropClass]->
(CC:CropClass)-[:hasCropSubClass]->
(CSC:CropSubClass)-[:hasCropDescription]->
(CD:CropDescription)-[:isAvailableIn]->
(R:Region)<-[:hasRegion]-(C:Country {CountryName: "Canada"})
MATCH (CSC)-[:hasCropVariety]->
(CV:CropVariety {CropVarietyName: "Zulu"})-[:hasCropDescription]->
(CD)-[:hasGrowthScale] ->
(GS:GrowthScale)-[:hasGrowthScaleStages]->(GSS:GrowthScaleStages)
MATCH (F:Fertilizers {ProdName: "Sulfan"})-[:isAvailableIn]->(R)
MATCH (F)-[:hasProdNutrient]->
(N:Nutrient)-[:hasNutrientUnit]->
(U:Units)-[:hasUnitConversion]->(UC:UnitConversion)
RETURN CG,CC,CSC,CD,CV,C,R,GS,GSS,F,N,U,UC LIMIT 300

MATCH (CG:CropGroup)-[:hasCropClass]->
(CC:CropClass)-[:hasCropSubClass]->
(CSC:CropSubClass {CropSubClassName: "Winter wheat"})-[:hasCropDescription]->
(CD:CropDescription)-[:isAvailableIn]->
(R:Region)<-[:hasRegion]-(C:Country {CountryName: "Canada"})
MATCH (CSC)-[:hasCropVariety]->
(CV:CropVariety)-[:hasCropDescription]->
(CD)-[:hasGrowthScale] ->
(GS:GrowthScale)-[:hasGrowthScaleStages]->(GSS:GrowthScaleStages)
MATCH (F:Fertilizers {ProdName: "Sulfan"})-[:isAvailableIn]->(R)
MATCH (F)-[:hasProdNutrient]->
(N:Nutrient)-[:hasNutrientUnit]->
(U:Units)-[:hasUnitConversion]->(UC:UnitConversion)
RETURN CG,CC,CSC,CD,CV,C,R,GS,GSS,F,N,U,UC LIMIT 300

MATCH (CG:CropGroup)-[:hasCropClass]->
(CC:CropClass {CropClassName: "Wheat"})-[:hasCropSubClass]->
(CSC:CropSubClass)-[:hasCropDescription]->
(CD:CropDescription)-[:isAvailableIn]->
(R:Region)<-[:hasRegion]-(C:Country {CountryName: "Canada"})
MATCH (CSC)-[:hasCropVariety]->
(CV:CropVariety)-[:hasCropDescription]->
(CD)-[:hasGrowthScale] ->
(GS:GrowthScale)-[:hasGrowthScaleStages]->(GSS:GrowthScaleStages)
MATCH (F:Fertilizers {ProdName: "Sulfan"})-[:isAvailableIn]->(R)
MATCH (F)-[:hasProdNutrient]->
(N:Nutrient)-[:hasNutrientUnit]->
(U:Units)-[:hasUnitConversion]->(UC:UnitConversion)
RETURN CG,CC,CSC,CD,CV,C,R,GS,GSS,F,N,U,UC LIMIT 300
