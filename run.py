# Example of Nuclei and Cell Segmentation Using UNSEG

import numpy as np
import matplotlib.pyplot as plt
from skimage import io
from skimage.segmentation import mark_boundaries

import unseg

def open_img(path_to_img):
    """Returns the RGB image (img) and two channels with
    nuclei (DAPI) and cell membrane (Na+K+ATPase) markers"""
    img = io.imread(path_to_img, plugin="tifffile")
    h = img.shape[0]
    w = img.shape[1]
    intensity = np.zeros((h,w,2), dtype='float64')
    intensity[:,:,0] = img[:,:,2] # Nuclei Marker
    intensity[:,:,1] = img[:,:,0] # Cell Membrane Marker
    return intensity, img

def plot_img(img, tlt='', cmp='gray'):
    """ Plot function """
    fig, ax = plt.subplots(nrows=1, ncols=1, figsize=(5,5))
    fig.subplots_adjust(left=0.0, bottom=0.0, right=1.0, top=1.0, wspace=None, hspace=None)
    ax.imshow(img, cmap=cmp)
    ax.set_title(tlt)
    ax.axis('on')
    plt.show()

# Path to image
path_to_img = './image/Gallbladder_Normal_Tissue.tif'

# Open and plot the original image
intensity, img = open_img(path_to_img)
plot_img(img, tlt='Image')

# Segment Nuclei and Cells
mask_nuclei, mask_cells, n_nuclei, n_cells = unseg.nuclei_cell_segmentation(
    intensity,
    area_threshold=20,
    convexity_threshold=4,
    cell_marker_threshold=25,
    dist_tr='GDT',
    sigma0=3,
    k0=1,
    r0=5,
    pct=[0.01, 0.01],
    nk=[5, 10, 20, 40],
    t0=0.5,
    ternary_met='Argmax',
    visualization=False,
    area_ratio_threshold=0.65,
    dilation_radius=5
)

# To segment nuclei and cells with default settings run the code as follows: 
# mask_nuclei, mask_cells, n_nuclei, n_cells = nuclei_cell_segmentation(intensity)

# Plot nuclei segmentation mask
plot_img(mark_boundaries(img, mask_nuclei, color=(1,1,1)), tlt='UNSEG Nuclei Segmentation: N = {0}'.format(n_nuclei))

# Plot cell segmentation mask
plot_img(mark_boundaries(img, mask_cells, color=(0,1,0)), tlt='UNSEG Cell Segmentation: N = {0}'.format(n_cells))

# Save nuclei and cell segmentations as txt files 
#np.savetxt('Segmentation_nuclei_{0}.txt'.format(n_nuclei), mask_nuclei, fmt='%d', delimiter=' ')
#np.savetxt('Segmentation_cells_{0}.txt'.format(n_cells), mask_cells, fmt='%d', delimiter=' ')
