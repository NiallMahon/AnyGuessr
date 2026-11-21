// Starts quizzes and submits guesses to the backend.
const BASE = '/api/quizzes';

/**
 * Data returned by the backend when a new quiz starts.
 * Includes the session identifier and initial target region.
 */
export interface StartQuizResponse {
  sessionId: string;
  mapId: string;
  currentTarget: string;
}

/**
 * Result of submitting a guess.
 *
 * Includes whether the guess was correct, whether a correction
 * is required, the updated score and quiz completion status.
 */
export interface GuessResponse {
  correct: boolean;
  correctionRequired: boolean;
  selectedRegionId: string;
  targetRegionId: string;
  currentTargetRegionId: string;
  correctAnswers: number;
  answeredTargets: number;
  accuracy: number;
  quizComplete: boolean;
}

async function post<T>(
  path: string,
  body: unknown
): Promise<T> {
  const response = await fetch(`${BASE}${path}`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(body),
  });

  if (!response.ok) {
    throw new Error(
      `Gameplay request failed: ${response.status}`
    );
  }

  return response.json() as Promise<T>;
}

/**
 * Requests a new quiz session for the selected map.
 *
 * @param mapId Identifier of the selected map.
 * @param userId Player identifier, or null when not provided.
 * @returns The new quiz session details.
 */
export function startQuiz(
  mapId: string,
  userId: number | null
): Promise<StartQuizResponse> {
  return post<StartQuizResponse>('', {
    mapId,
    userId,
  });
}

/**
 * Sends a player's selected region to the backend for evaluation.
 *
 * @param sessionId Identifier of the active quiz session.
 * @param selectedRegionId Region selected by the player.
 * @param clickedLat Optional latitude of the click.
 * @param clickedLng Optional longitude of the click.
 * @returns The guess result and updated quiz progress.
 */
export function submitGuess(
  sessionId: string,
  selectedRegionId: string,
  clickedLat?: number,
  clickedLng?: number
): Promise<GuessResponse> {
  return post<GuessResponse>(
    `/${sessionId}/guesses`,
    {
      selectedRegionId,
      clickedLat,
      clickedLng,
    }
  );
}