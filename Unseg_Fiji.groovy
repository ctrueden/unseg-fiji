#@ Img image
#@output Img nuclei
#@output Img cells

import org.apposed.appose.Appose

println("== BUILDING ENVIRONMENT ==")
pixiToml = """
[workspace]
authors = ["Curtis Rueden <ctrueden@wisc.edu>"]
channels = ["conda-forge"]
name = "unseg-fiji"
platforms = ["linux-64"]
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
	inputs = ["ndarray": imgToAppose(image)]
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
