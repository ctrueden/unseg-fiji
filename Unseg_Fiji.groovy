#@ Img image

#@ Integer (value=20) area_threshold
#@ Integer (value=4) convexity_threshold
#@ Integer (value=25) cell_marker_threshold
#@ String (value='GDT') dist_tr
#@ Integer (value=3) sigma0
#@ Integer (value=1) k0
#@ Integer (value=5) r0
#@ String (value="0.01, 0.01") pct
#@ String (value="5, 10, 20, 40") nk
#@ Double (value=0.5) t0
#@ String (value='Argmax') ternary_met
#@ Boolean (value=false) visualization
#@ Double (value=0.65) area_ratio_threshold
#@ Integer (value=5) dilation_radius

#@output Img nuclei
#@output Img cells

import org.apposed.appose.Appose

println("== BUILDING ENVIRONMENT ==")
pixiToml = """
[workspace]
authors = ["Curtis Rueden <ctrueden@wisc.edu>"]
channels = ["conda-forge"]
name = "unseg-fiji"
platforms = ["linux-aarch64", "linux-64", "osx-arm64", "osx-64", "win-64"]
version = "0.1.0"

[tasks]

[dependencies]
python = "3.9.*"
appose = "==0.7.2"
numpy = "==1.24.3"
matplotlib = "==3.7.1"
scikit-image = "==0.20.0"
scikit-learn = "==1.2.2"
scipy = "==1.9.1"

[pypi-dependencies]
opencv-python-headless = "==4.7.0.72"
"""

env = Appose.pixi().content(pixiToml).logDebug().build()
println("Environment build complete: ${env.base()}")

// Read in the Python script (TODO: load as resource instead of hardcoding path)
unsegPath = System.getProperty("user.home") + "/Desktop/unseg-fiji/unseg.py"
unsegScript = new File(unsegPath).text
println("Loaded unseg script of length ${unsegScript.length()}")

// Conversion functions: ImgLib2 Img <-> Appose NDArray
import net.imglib2.appose.ShmImg
imgToAppose = { img ->
	ndArray = ShmImg.copyOf(image).ndArray()
	println("Copied image into shared memory: ${ndArray.shape()}")
	return ndArray
}
import net.imglib2.appose.NDArrays
apposeToImg = { ndarray ->
	NDArrays.asArrayImg(ndarray)
}

// Run the script as an Appose task
println("== STARTING PYTHON SERVICE ==")
try (python = env.python()) {
	inputs = [
		"ndarray": imgToAppose(image),
		"area_threshold": area_threshold,
		"convexity_threshold": convexity_threshold,
		"cell_marker_threshold": cell_marker_threshold,
		"dist_tr": dist_tr,
		"sigma0": sigma0,
		"k0": k0,
		"r0": r0,
		"pct": pct.split(', ')*.toDouble() as double[],
		"nk": nk.split(', ')*.toInteger() as int[],
		"t0": t0,
		"ternary_met": ternary_met,
		"visualization": visualization,
		"area_ratio_threshold": area_ratio_threshold,
		"dilation_radius": dilation_radius,
	]
	task = python.task(unsegScript, inputs)
		.listen { if (it.message) println("[UNSEG] ${it.message}") }
		.waitFor()

	println("TASK FINISHED: ${task.status}")
	if (task.error) println(task.error)
	nuclei = NDArrays.asArrayImg(task.outputs["nuclei"])
	cells = NDArrays.asArrayImg(task.outputs["cells"])
}
finally {
	println("== TERMINATING PYTHON SERVICE ==")
}
