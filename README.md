# Chess Coach

Android chess training/review app. Current first version includes:
- interactive chessboard with legal move validation (chess.js loaded from CDN)
- undo, new game, board flip
- check, checkmate, stalemate and draw status
- FEN import/copy
- recognition of a few common openings from the move sequence

## Build APK
Open the Actions tab and run **Build Chess Coach APK**, or wait for the workflow after pushing to main. Download the `Chess-Coach-debug-apk` artifact from a successful run.

## Current limitations
This is an MVP. It does not yet include Stockfish engine analysis, a full opening database, or reliable brilliant-move classification. Internet is required to load chess.js in this version.
