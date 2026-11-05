# 🐦 Counting Birds in Park Photos

![Status](https://img.shields.io/badge/Status-Work_in_Progress-orange?style=flat-square)
![Domain](https://img.shields.io/badge/Domain-Digital_Image_Processing-blue?style=flat-square)
![Focus](https://img.shields.io/badge/Focus-Computer_Vision-success?style=flat-square)

<p align="center">
  <img src="Neon Bird Vision Analysis Collage.png" alt="Project Banner Image">
</p>

> **Note:** This repository is currently a placeholder. Future commits will contain the Python/Java source code, datasets, and evaluation metrics for this project.

## 📖 Overview
This repository focuses on estimating bird populations in park images using algorithmic digital image processing. Because outdoor environments contain complex backgrounds, this project relies on foundational image analysis techniques to process digital images, segment foreground objects, and extract regional features.

## 🧩 The Problem
Counting targets in a natural environment requires specific steps to isolate the subjects from background noise. When using traditional digital image processing, we must systematically transform the raw pixel values into structured geometric data. This project will explore a pipeline that moves from basic pixel manipulation to identifying connected regions in binary images.
---

## ⚙️ Proposed Processing Pipeline

Based on established digital image processing workflows, the project will implement the following sequential techniques:

### 1. Image Pre-processing and Filtering 🧽
Before birds can be separated from the background, the raw digital image must be enhanced and cleaned of outdoor noise.
* **Point Operations:** We will modify image intensity and adjust contrast to handle varying lighting conditions across different park photos.
* **Linear and Nonlinear Filters:** We will apply smoothing operations, such as Gaussian filters or median filters, to reduce image noise while attempting to preserve the structure of the image.

### 2. Image Segmentation ✂️
The core challenge is separating the birds (the foreground) from the park background (trees, sky, grass).
* **Automatic Thresholding:** We will utilize global histogram-based thresholding methods to binarize the image, turning potential birds into solid regions.
* **Threshold Selection:** We will implement automatic threshold selection algorithms, such as Otsu's Method or the Isodata Algorithm, to automatically find the optimal pixel intensity cutoff for isolating the birds.

### 3. Morphological Cleanup 🧹
Thresholding often leaves imperfect shapes, such as disconnected bird parts or noisy background artifacts.
* **Basic Morphological Operations:** We will apply dilation to grow and connect fragmented bird segments, and erosion to shrink and remove isolated background noise pixels.
* **Composite Operations:** We will use opening and closing techniques to smooth the boundaries of the detected birds and separate subjects that might be slightly overlapping.

### 4. Region Counting and Analysis 🧮
Once the image is cleanly binarized and morphologically filtered, the system must count the distinct objects.
* **Region Labeling:** We will find connected image regions utilizing algorithms like sequential region labeling or flood filling to identify each distinct bird as a unique blob.
* **Shape Properties:** We will calculate statistical shape properties for each binary region, such as the centroid and the bounding box, to verify if the detected region matches the expected geometric features of a bird.

### 5. Advanced Matching Techniques 🎯 (Optional - Depends on our time availability)
For more complex scenarios involving varying bird sizes or distinct species shapes, the project may explore feature-based matching.
* **Template Matching:** We will explore template matching in intensity images to calculate the distance between image patterns and find specific bird shapes.
* **Scale-Invariant Local Features (SIFT):** We will look into extracting interest points at multiple scales to detect birds regardless of whether they are close to the camera or distant in the sky.
