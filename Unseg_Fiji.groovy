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
