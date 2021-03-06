CSGDatabase.clear()

LengthParameter encoderBoltKeepaway 		= new LengthParameter("encoderBoltKeepaway",0,[1.2,0])
encoderBoltKeepaway.setMM(0)
CSG encoderBearing = (CSG)ScriptingEngine
	                    .gitScriptRun(
                                "https://github.com/madhephaestus/SeriesElasticActuator.git", // git location of the library
	                              "encoderBoard.groovy" , // file to load
	                              null
                        )
                     
LengthParameter printerOffset 		= new LengthParameter("printerOffset",0.5,[1.2,0])                
LengthParameter boltLen 		= new LengthParameter("Bolt Length",0.5,[1.2,0])                
double shelThickness = 2		

double vexGrid = (1.0/2.0)* 25.4
double overEncoder = encoderBearing.getMaxZ()
double underEncoder = Math.abs(encoderBearing.getMinZ())
double totalEncoder = overEncoder+underEncoder+shelThickness
double bearingRadius = encoderBearing.getMaxX()
boltLen.setMM(totalEncoder+shelThickness)
CSG vshaft =  (CSG)ScriptingEngine
	                    .gitScriptRun(
                                "https://github.com/WPIRoboticsEngineering/RBELabCustomParts.git", // git location of the library
	                              "vexShaft.groovy" , // file to load
	                              [11]
                        )
				.toZMin()
				.movez(-1)  
CSG vshaftLong =  (CSG)ScriptingEngine
	                    .gitScriptRun(
                                "https://github.com/WPIRoboticsEngineering/RBELabCustomParts.git", // git location of the library
	                              "vexShaft.groovy" , // file to load
	                              [11+totalEncoder]
                        )
				.toZMin()
				.movez(-1)
CSG allignment = Vitamins.get("vexFlatSheet","Aluminum 5x15")	
				.rotz(90)
				
allignment=allignment
			.movey(vexGrid*3)	
			.movex(-vexGrid*2)	
			.movez(	totalEncoder-printerOffset.getMM())

CSG bolt = Vitamins.get("capScrew","8#32")
			.makeKeepaway(printerOffset.getMM()*1.5)
			.movez(- printerOffset.getMM()/2 -0.5)
			
double headHeight =bolt.getMaxZ() 
CSG encoderBearingPlaced = encoderBearing
						.rotx(180)
						.toZMin()
						.movez(shelThickness)
CSG totalBolt= bolt				
for(double i=headHeight;i<(totalEncoder+headHeight);i+=headHeight){
	
	totalBolt=totalBolt.union(bolt.movez(i))
}
double gridOffset = 2
CSG bolts = CSG.unionAll([totalBolt.movey(vexGrid*gridOffset),
					totalBolt.movey(-vexGrid*gridOffset)])
			.movez(totalEncoder+shelThickness)
bolts=CSG.unionAll([bolts,
				bolts.movex(vexGrid),
				bolts.movex(-vexGrid)])
CSG bearingLug=new Cylinder(bearingRadius+shelThickness,totalEncoder).toCSG() 
CSG boltLug=new Cylinder(3+shelThickness,totalEncoder).toCSG() 
boltLug=CSG.unionAll([boltLug,
				boltLug.movex(vexGrid),
				boltLug.movex(-vexGrid)])

double shaftflange = 9
CSG center = new Cylinder(shaftflange-shelThickness,totalEncoder).toCSG()
CSG shaftCutter=new Cylinder(shaftflange,totalEncoder).toCSG() 
				.union(new Cylinder(bearingRadius,shelThickness).toCSG() )	
				.difference(center)
CSG pinKeepaway = new Cube(22,3.75,2.15).toCSG()
				.toZMax()
				.movez(		totalEncoder)
				.movey(17.86)		
CSG pinKeepawayLug = pinKeepaway.makeKeepaway(shelThickness)
				.toZMax()
				.movez(		totalEncoder)
	
CSG core = bearingLug
		.union(boltLug.movey(vexGrid*gridOffset))
		.union(boltLug.movey(-vexGrid*gridOffset))
		.union(pinKeepawayLug)
		.hull()
		.difference(encoderBearingPlaced)
		.difference(bolts)
		.difference(shaftCutter)	
		.difference(vshaft)	
		.difference(pinKeepaway)	
		.intersect(new Cube(vexGrid*3,vexGrid*gridOffset*4,totalEncoder).toCSG().toZMin())
CSG bearingBracket =  core.difference(center).rotx(180)// fix the orentation
			.toZMin()//move it down to the flat surface
			.minkowskiDifference(allignment,printerOffset.getMM())
			
CSG pin =  core.intersect(center.movez(printerOffset.getMM()*2))
			.toZMin()
			
CSG passThrough = pin.difference(vshaftLong)	
				

CSG magnetVariant =CSG.unionAll([bearingBracket,pin])
magnetVariant	.setName("magnetPin")

CSG patssThroughVariant =CSG.unionAll([bearingBracket,passThrough])
patssThroughVariant	.setName("passThroughPin")
return   [magnetVariant,patssThroughVariant]