# Copyright (c) 2025 Brian Kircher
#
# Open Source Software; you can modify and/or share it under the terms of BSD
# license file in the root directory of this project.

project = 'FLL Scorer'
copyright = '2025, Brian Kircher'
author = 'Brian Kircher'
extensions = [ 'sphinx_rtd_theme', 'sphinx.ext.githubpages', 'sphinx_fontawesome' ]
templates_path = [ '_templates' ]
exclude_patterns = [ '_build', 'Thumbs.db', '.DS_Store' ]
nitpicky = True
html_theme = 'sphinx_rtd_theme'
html_title = 'FLL Scorer'
html_copy_source = False
html_show_sourcelink = False
html_static_path = [ '_static' ]
html_css_files = [ 'custom.css' ]
html_favicon = '_static/favicon.ico'